package org.example.service;

import org.example.dao.*;
import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.dto.BidResult;
import org.example.entity.Auction;
import org.example.entity.BidHistory;
import org.example.entity.BidTransaction;
import org.example.entity.item.Item;
import org.example.entity.user.User;
import org.example.exception.AuctionSystemException;
import org.example.exception.auction.AuctionClosedException;
import org.example.exception.auction.AuctionNotFoundException;
import org.example.exception.auction.InvalidAuctionTimeException;
import org.example.exception.auth.UserNotFoundException;
import org.example.exception.bid.InvalidBidException;
import org.example.exception.database.DatabaseException;
import org.example.exception.item.InvalidItemPriceException;
import org.example.exception.item.InvalidItemStateException;
import org.example.exception.item.ItemNotFoundException;
import org.example.utils.ConfigManager;
import org.example.utils.DatabaseConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class AuctionService {

    private AuctionDAO      auctionDAO    = new AuctionDAO();
    private BidTransactionDAO bidDAO      = new BidTransactionDAO();
    private ItemDAO         itemDAO       = new ItemDAO();
    private UserDAO         userDAO       = new UserDAO();
    private BidHistoryDAO   bidHistoryDAO = new BidHistoryDAO();
    private AuctionEngine   engine;
    private AutoBidService  autoBidService;

    private final ConcurrentHashMap<Integer, ReentrantLock> auctionLocks = new ConcurrentHashMap<>();

    // ── Anti-snipe config (đọc từ application.properties) ────────────────────
    private final int SNIPE_THRESHOLD_SECONDS;
    private final int EXTEND_SECONDS;

    private static AuctionService instance;

    private AuctionService() {
        ConfigManager cfg = ConfigManager.getInstance();
        this.SNIPE_THRESHOLD_SECONDS = cfg.getInt("auction.antisnipe.trigger_seconds", 60);
        this.EXTEND_SECONDS          = cfg.getInt("auction.antisnipe.extend_seconds",  120);
        System.out.println("[AUCTION] Anti-snipe config: trigger="
                + SNIPE_THRESHOLD_SECONDS + "s | extend=" + EXTEND_SECONDS + "s");
    }

    public static synchronized AuctionService getInstance() {
        if (instance == null) instance = new AuctionService();
        return instance;
    }

    public void setEngine(AuctionEngine engine)               { this.engine = engine; }
    public void setAutoBidService(AutoBidService s)           { this.autoBidService = s; }

    public ReentrantLock getLock(int auctionId) {
        return auctionLocks.computeIfAbsent(auctionId, id -> new ReentrantLock(true));
    }

    // ── Mở phiên đấu giá ─────────────────────────────────────────────────────

    public void openAuction(int itemId, int sellerId, BigDecimal startingPrice, BigDecimal stepPrice, LocalDateTime startTime, LocalDateTime endTime) {
        Item item = itemDAO.getItemById(itemId);
        if (item == null)
            throw new ItemNotFoundException("Loi: Khong thay mon hang co ID = " + itemId);
        if (!item.getStatus().equals("AVAILABLE"))
            throw new InvalidItemStateException("Loi: San pham hien dang khong available");
        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            throw new IllegalArgumentException("Lỗi: Thời gian bắt đầu phải diễn ra trước thời gian kết thúc!");
        }
        if (startTime.isBefore(LocalDateTime.now().minusMinutes(2))) {
            throw new InvalidAuctionTimeException("Lỗi: Không được set thời gian bắt đầu trong quá khứ!");
        }
        if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) throw new InvalidItemPriceException("Lỗi: Giá khởi tạo không hợp lệ");

        Auction newAuction = new Auction();
        newAuction.setItemId(itemId);
        newAuction.setSellerId(sellerId);
        newAuction.setStartingPrice(startingPrice);
        newAuction.setStepPrice(stepPrice);
        newAuction.setStartTime(startTime);
        newAuction.setEndTime(endTime);
        newAuction.setSellerId(sellerId);
        newAuction.setCurrentPrice(startingPrice);
        newAuction.setItem(item);

        if (!startTime.isAfter(LocalDateTime.now())) {
            newAuction.setStatus("RUNNING");
        } else {
            newAuction.setStatus("OPEN");
        }

        int newAuctionId = auctionDAO.createAuction(newAuction);
        if (newAuctionId <= 0)
            throw new AuctionSystemException("Loi khi mo Auction");
            
        newAuction.setId(newAuctionId);

        itemDAO.updateItemStatus(itemId, "IN_AUCTION");

        if (this.engine != null) {
            this.engine.addAuction(newAuction);
        } else {
            throw new AuctionSystemException("[AUCTION] AuctionEngine chua duoc inject!");
        }
    }

    // ── Đặt giá ───────────────────────────────────────────────────────────────

    /**
     * Đặt giá cho một phiên đấu giá.
     *
     * @return BidResult chứa kết quả thành công/thất bại và thông tin gia hạn nếu có.
     * @throws DatabaseException nếu có lỗi kỹ thuật (DB, mạng...)
     */
    public BidResult placeBid(int bidderId, int auctionId, BigDecimal bidAmount) {
        ReentrantLock lock = getLock(auctionId);
        lock.lock();
        Connection conn = null;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            conn.setAutoCommit(false);

            // ==========================================================
            // I. KIỂM TRA ĐIỀU KIỆN (VALIDATION)
            // ==========================================================
            Auction auction = auctionDAO.getAuctionById(auctionId);

            if (auction == null) throw new AuctionNotFoundException("Phien dau gia khong ton tai.");

            if (!"RUNNING".equals(auction.getStatus())) {
                // Nếu phiên đang OPEN nhưng đã qua startTime → tự chuyển sang RUNNING
                // (AuctionEngine chạy mỗi 5s nên có thể chưa kịp cập nhật)
                if ("OPEN".equals(auction.getStatus()) 
                        && auction.getStartTime() != null 
                        && !LocalDateTime.now().isBefore(auction.getStartTime())) {
                    auctionDAO.updateAuctionStatus("RUNNING", auctionId);
                    auction.setStatus("RUNNING");
                    System.out.println("[BID] Tu dong chuyen phien " + auctionId + " tu OPEN sang RUNNING (da qua startTime)");
                    
                    // Cập nhật object trên RAM cho Engine
                    if (engine != null) {
                        Auction liveAuction = engine.findActiveAuctionById(auctionId);
                        if (liveAuction != null) {
                            liveAuction.setStatus("RUNNING");
                        }
                    }
                } else {
                    throw new InvalidBidException("Phòng chưa mở hoặc đã kết thúc!");
                }
            }

            if (auction.getEndTime().isBefore(LocalDateTime.now())) {
                throw new AuctionClosedException("Hết giờ! Không thể đặt giá thêm.");
            }

            if (bidAmount.compareTo(auction.getCurrentPrice()) <= 0) {
                throw new InvalidBidException("Giá đặt phải cao hơn giá hiện tại: " + auction.getCurrentPrice());
            }

            if (auction.getSellerId() == bidderId) {
                throw new InvalidBidException("Lỗi: Không được tự bid sản phẩm của chính mình!");
            }

            User bidder = userDAO.getUserById(bidderId);
            if (bidder == null) throw new UserNotFoundException("Không tìm thấy người dùng");

            if (!"BIDDER".equals(bidder.getRole())) {
                throw new InvalidBidException("Lỗi: Bạn không có quyền đặt giá!");
            }

            BigDecimal current = auction.getCurrentPrice();
            BigDecimal step = auction.getStepPrice();
            BigDecimal minRequired = current.add(step);

            if (bidAmount.compareTo(minRequired) < 0) {
                throw new InvalidBidException("Lỗi: Giá đặt thấp hơn giá hiện tại");
            }


            // ==========================================================
            // II. XỬ LÝ GIAO DỊCH CHÍNH (TRANSACTION)
            // ==========================================================

            boolean isDeducted = userDAO.deductBalance(conn, bidderId, bidAmount);
            if (!isDeducted) {
                throw new InvalidBidException("Số dư không đủ! (Lỗi giao dịch)");
            }

            BidTransaction highestBid = bidDAO.getHighestBid(auctionId);
            if (highestBid != null) {
                userDAO.addBalance(conn, highestBid.getBidderId(), highestBid.getBidPrice());
            }

            auctionDAO.updateCurrentPrice(conn, auctionId, bidAmount);

            BidTransaction newTx = new BidTransaction();
            newTx.setAuctionId(auctionId);
            newTx.setBidderId(bidderId);
            newTx.setBidPrice(bidAmount);
            newTx.setBidTime(LocalDateTime.now());
            bidDAO.addBid(conn, newTx);

            BidHistory entry = new BidHistory();
            entry.setAuctionId(auctionId);
            entry.setBidderId(bidderId);
            entry.setPrice(bidAmount);
            entry.setBidTime(LocalDateTime.now());
            bidHistoryDAO.addBidHistory(conn, entry);

            // ==========================================================
            // III. XỬ LÝ ANTI-SNIPE (GIA HẠN THỜI GIAN)
            // ==========================================================
            long secondsLeft = ChronoUnit.SECONDS.between(LocalDateTime.now(), auction.getEndTime());
            boolean isExtended = false;
            LocalDateTime newEndTime = auction.getEndTime();

            if (secondsLeft > 0 && secondsLeft <= SNIPE_THRESHOLD_SECONDS) {
                newEndTime = auction.getEndTime().plusSeconds(EXTEND_SECONDS);

                // Cập nhật DB (TRONG TRANSACTION)
                auctionDAO.updateEndTime(conn, auctionId, newEndTime);
                isExtended = true;

                System.out.println("[ANTI-SNIPE] Phien " + auctionId + " gia han them " + EXTEND_SECONDS + "s");
            }

            // ==========================================================
            // IV. CHỐT GIAO DỊCH (COMMIT)
            // ==========================================================
            conn.commit();

            // ==========================================================
            // V. HẬU KỲ (THAO TÁC KHÔNG ẢNH HƯỞNG DATA CORE)
            // ==========================================================
            // Trigger auto-bid bot sau khi đã chốt data thành công
            autoBidService.triggerAsync(auctionId, bidderId);

            // Cập nhật Object trên RAM cho Engine chạy nền
            if (isExtended && engine != null) {
                Auction liveAuction = engine.findActiveAuctionById(auctionId);
                if (liveAuction != null) {
                    liveAuction.setEndTime(newEndTime);
                }
            }

            // Trả kết quả
            if (isExtended) {
                return BidResult.successWithExtension(EXTEND_SECONDS, newEndTime);
            }
            return BidResult.success();

        } catch (AuctionSystemException e) {
            System.err.println("[BID] Loi nghiep vu: " + e.getMessage());
            rollback(conn);
            return BidResult.failure(e.getMessage());
        } catch (Exception e) {
            System.err.println("[BID] Loi he thong: " + e.getMessage());
            rollback(conn);
            throw new DatabaseException("Loi he thong trong giao dich dat gia", e);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    System.err.println("[BID] Loi dong connection: " + e.getMessage());
                }
            }
            lock.unlock();
        }
    }

    // ── Đóng phiên đấu giá ───────────────────────────────────────────────────

    public boolean closeAuction(int auctionId) {
        ReentrantLock lock = getLock(auctionId);
        lock.lock();
        try {
            Auction auction = auctionDAO.getAuctionById(auctionId);
            if (auction == null)
                throw new AuctionNotFoundException("Loi: Khong ton tai auction");
            if (!"RUNNING".equals(auction.getStatus()) && !"OPEN".equals(auction.getStatus()))
                throw new AuctionClosedException("Loi: Auction da ket thuc hoac bi huy");

            BidTransaction highestBid = bidDAO.getHighestBid(auctionId);
            if (highestBid != null) {
                auctionDAO.closeAuction(auctionId, highestBid.getBidderId());
                itemDAO.updateItemStatus(auction.getItemId(), "SOLD");
            } else {
                auctionDAO.updateAuctionStatus("CANCELED", auctionId);
                itemDAO.updateItemStatus(auction.getItemId(), "AVAILABLE");
            }
            return true;

        } catch (AuctionSystemException e) {
            System.err.println("[CLOSE] Loi nghiep vu: " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("[CLOSE] Loi ky thuat: " + e.getMessage());
            return false;
        } finally {
            lock.unlock();
            auctionLocks.remove(auctionId);
        }
    }

    // ── Lịch sử bid ──────────────────────────────────────────────────────────

    public List<BidHistory> getBidHistory(int auctionId) {
        if (auctionDAO.getAuctionById(auctionId) == null)
            throw new AuctionNotFoundException("Loi: Khong tim thay auction co ID: " + auctionId);
        return bidHistoryDAO.getHistoryByAuctionId(auctionId);
    }
    // ── Lấy chi tiết phiên đấu giá ──────────────────────────────────────────
    public Auction getAuctionById(int auctionId) {
        return auctionDAO.getAuctionById(auctionId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private void rollback(Connection conn) {
        if (conn != null) {
            try { conn.rollback(); }
            catch (SQLException ex) {
                System.err.println("[BID] Loi rollback: " + ex.getMessage());
            }
        }
    }

}