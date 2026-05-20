package org.example.service;

import org.example.entity.Auction;
import org.example.observer.BidObserver;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quản lý toàn bộ AuctionRoom đang hoạt động.
 * Được inject vào AuctionEngine và các Controller.
 */
public class AuctionRoomManager {

    private final Map<Integer, AuctionRoom> rooms = new ConcurrentHashMap<>();

    /** Lấy hoặc tạo phòng cho phiên — gọi khi phiên bắt đầu */
    public AuctionRoom getOrCreateRoom(Auction auction) {
        return rooms.computeIfAbsent(
                auction.getId(),
                id -> new AuctionRoom(id, auction.getName())
        );
    }

    /** Lấy phòng theo ID — trả null nếu chưa tạo */
    public AuctionRoom getRoom(int auctionId) {
        return rooms.get(auctionId);
    }

    /** Xóa phòng khi phiên kết thúc */
    public void removeRoom(int auctionId) {
        rooms.remove(auctionId);
    }

    /** Dọn observer khỏi tất cả phòng — gọi khi client ngắt kết nối */
    public void clearObserverFromAllRooms(BidObserver observer) {
        for (AuctionRoom room : rooms.values()) {
            room.removeObserver(observer);
        }
    }
    /** Client JOIN phòng — tạo phòng nếu chưa có, rồi đăng ký observer */
    public void joinRoom(Auction auction, BidObserver observer) {
        AuctionRoom room = getOrCreateRoom(auction);
        room.addObserver(observer);
    }
}