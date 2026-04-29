package org.example;

import com.auction.core.engine.AuctionEngine;
import com.auction.core.model.Auction;
import com.auction.core.model.Item;
import com.auction.core.network.AuctionServer;

public class Main {
    public static void main(String[] args) {
        System.out.println("Bắt đầu khởi động toàn bộ Hệ thống...");

        // 1. Bật Động cơ đa luồng (Engine)
        AuctionEngine engine = new AuctionEngine();
        engine.startEngine();

        // (Tùy chọn) Khởi tạo sẵn một món hàng để test
        Item item1 = new Item("Laptop Gaming ROG");
        // Lấy thời gian ngay lúc này cộng thêm 30 giây để làm hạn chót
        Auction auction1 = new Auction(item1, 5000, java.time.LocalDateTime.now().plusSeconds(30));
        engine.addAuction(auction1);

        // 2. Mở cổng Server ở Port 8080 và gắn Động cơ vào
        AuctionServer server = new AuctionServer(8080, engine);
        server.start(); // Hàm này chứa vòng lặp vô tận, chương trình sẽ luôn chạy ở đây
    }
}