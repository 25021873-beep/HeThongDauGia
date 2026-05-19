package org.example.service;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.Auction;
import org.example.entity.AutoBidConfig;
import org.example.entity.user.User;
import org.example.exception.auction.AuctionClosedException;
import org.example.exception.auction.AuctionNotFoundException;
import org.example.exception.bid.InsufficientBalanceException;
import org.example.exception.bid.InvalidBidException;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AutoBidService {

    private final ExecutorService autoBidExecutor = Executors.newFixedThreadPool(4);
    private final AutoBidDAO autoBidDAO;
    private final AuctionService auctionService;
    private final AuctionDAO auctionDAO;
    private final UserDAO userDAO;


    public AutoBidService(AutoBidDAO autoBidDAO, AuctionService auctionService,
                          AuctionDAO auctionDAO, UserDAO userDAO) {
        this.autoBidDAO      = autoBidDAO;
        this.auctionService  = auctionService;
        this.auctionDAO      = auctionDAO;
        this.userDAO         = userDAO;
    }

    // Hàm đăng ký auto-bid
    public synchronized void registerAutoBid(int bidderId, int auctionId, BigDecimal maxBid, BigDecimal increment) {
        Auction auction = auctionDAO.getAuctionById(auctionId);
        if (auction == null) throw new AuctionNotFoundException("Lỗi: Không tìm thấy auction có id: " +  auctionId);
        if (auction.getStatus() == "FINISHED") throw new AuctionClosedException("Lỗi: Auction có id: " + auctionId + " đã đóng");

        if (maxBid.compareTo(auction.getCurrentPrice()) <= 0) {
            throw new InvalidBidException("maxBid phai lon hon gia hien tai: " + auction.getCurrentPrice());
        }
        if (increment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidBidException("Buoc gia phai lon hon 0");
        }

        User bidder = userDAO.getUserById(bidderId);
        if (bidder.getBalance().compareTo(maxBid) < 0) {
            throw new InsufficientBalanceException("Lỗi: Balance không được phép âm");
        }

        AutoBidConfig config = new AutoBidConfig();
        config.setAuctionId(auctionId);
        config.setBidderId(bidderId);
        config.setMaxBid(maxBid);
        config.setIncrement(increment);
        autoBidDAO.saveOrUpdate(config);
    }

    // Hàm Trigger sau mỗi bid thành công
    public void triggerAutoBid(int auctionId, int triggerBidderId) {
        Auction auction = auctionDAO.getAuctionById(auctionId);
        if (auction == null || auction.getStatus() != "RUNNING") return;

        List<AutoBidConfig> configs = autoBidDAO.getActiveAutoBids(auctionId);

        for (AutoBidConfig config : configs) {
            // Bỏ qua người vừa bid — không tự bid lại chính mình
            if (config.getBidderId() == triggerBidderId) continue;

            BigDecimal nextPrice = auction.getCurrentPrice().add(config.getIncrement());

            // Vượt quá maxBid → vô hiệu hóa, bỏ qua
            if (nextPrice.compareTo(config.getMaxBid()) > 0) {
                autoBidDAO.deactivate(auctionId, config.getBidderId());
                continue;
            }

            // Kiểm tra số dư
            User bidder = userDAO.getUserById(config.getBidderId());
            if (bidder.getBalance().compareTo(nextPrice) < 0) {
                autoBidDAO.deactivate(auctionId, config.getBidderId());
                continue;
            }

            // Đặt giá tự động — dùng lại placeBid() đã có sẵn
            try {
                auctionService.placeBid(config.getBidderId(), auctionId, nextPrice);

                // Reload auction sau khi bid để kiểm tra lại giá mới
                auction = auctionDAO.getAuctionById(auctionId);

            } catch (InvalidBidException | InsufficientBalanceException e) {
                // Giá không còn hợp lệ nữa → deactivate
                autoBidDAO.deactivate(auctionId, config.getBidderId());
            }
        }
    }

    // Hàm submit task vào Pool
    public void triggerAsync(int auctionId, int triggerBidderId) {
        autoBidExecutor.submit(() -> {
            try {
                triggerAutoBid(auctionId, triggerBidderId);
            } catch (Exception e) {
                System.err.println("[AUTO-BID] Loi khi trigger: " + e.getMessage());
            }
        });
    }

    // Hàm tắt Executor Service
    public void shutdown() {
        autoBidExecutor.shutdown();
        try {
            if (!autoBidExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                autoBidExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            autoBidExecutor.shutdownNow();
        }
    }
}
