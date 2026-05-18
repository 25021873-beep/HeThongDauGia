package org.example.service;

import org.example.entity.Auction;
import org.example.observer.BidObserver;

import java.util.concurrent.ConcurrentHashMap;

/**
 * AuctionRoomManager quản lý toàn bộ các phòng đấu giá đang hoạt động.
 * Đây là trung tâm điều phối việc tham gia/rời phòng và phát thông báo.
 */
public class AuctionRoomManager {
    // Quản lý: ID Phiên -> AuctionRoom tương ứng
    private final ConcurrentHashMap<Integer, AuctionRoom> activeRooms = new ConcurrentHashMap<>();

    /** Tạo hoặc lấy phòng đấu giá hiện có */
    public AuctionRoom getOrCreateRoom(Auction auction) {
        return activeRooms.computeIfAbsent(auction.getId(), id -> new AuctionRoom(auction));
    }

    /** Người dùng tham gia xem một phiên */
    public void joinRoom(Auction auction, BidObserver observer) {
        getOrCreateRoom(auction).addObserver(observer);
    }

    /** Người dùng rời một phiên cụ thể */
    public void leaveRoom(int auctionId, BidObserver observer) {
        AuctionRoom room = activeRooms.get(auctionId);
        if (room != null) {
            room.removeObserver(observer);
        }
    }

    /** Hủy đăng ký khỏi tất cả các phòng (dùng khi client ngắt kết nối) */
    public void clearObserverFromAllRooms(BidObserver observer) {
        for (AuctionRoom room : activeRooms.values()) {
            room.removeObserver(observer);
        }
    }

    /** Xóa phòng khi phiên đấu giá kết thúc */
    public void removeRoom(int auctionId) {
        activeRooms.remove(auctionId);
    }

    public AuctionRoom getRoom(int auctionId) {
        return activeRooms.get(auctionId);
    }
}