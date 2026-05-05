package com.auction;

import com.auction.core.engine.AuctionEngine;
import com.auction.core.model.Auction;
import com.auction.core.model.Item;
import com.auction.core.network.AuctionServer;

import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {

        AuctionEngine engine = new AuctionEngine();


        createMockData(engine);


        engine.startEngine();
        System.out.println("[SYSTEM] Auction Engine đã bắt đầu chạy...");


        int port = 8080;
        AuctionServer server = new AuctionServer(port, engine);

        System.out.println("[SYSTEM] Server đang khởi động tại cổng " + port + "...");
        server.start();
    }

    private static void createMockData(AuctionEngine engine) {

        Item laptop = new Item("LaptopDell", "Laptop Gaming G15", 1500.0);
        Auction a1 = new Auction(laptop,1500, LocalDateTime.now().plusMinutes(5)); // Kết thúc sau 5 phút


        Item phone = new Item("iPhone15", "iPhone 15 Pro Max 256GB", 1200.0);
        Auction a2 = new Auction(phone,1200, LocalDateTime.now().plusMinutes(10)); // Kết thúc sau 10 phút


        engine.addAuction(a1);
        engine.addAuction(a2);

        System.out.println("[DATA] Đã khởi tạo thành công 2 phiên đấu giá mẫu.");
    }
}