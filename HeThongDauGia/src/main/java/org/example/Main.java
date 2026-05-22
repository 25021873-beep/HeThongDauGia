package org.example;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.user.UserDAO;
import org.example.network.AuctionServer;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;
import org.example.service.UserService;

import java.io.InputStream;
import java.util.Properties;


public class Main {
    public static void main(String[] args) {
        System.out.println("=============================================");
        System.out.println("   KHOI DONG HE THONG DAU GIA TRUC TUYEN     ");
        System.out.println("=============================================\n");

        try {
            // -------------------------------------------------------------
            // BƯỚC 1: ĐỌC PORT TỪ FILE CONFIG (application.properties)
            // -------------------------------------------------------------
            System.out.print("[1/4] Doc cau hinh he thong... ");
            Properties props = new Properties();
            int port = 8080; // Default port nếu lỡ file config bị lỗi

            // Dùng ClassLoader để lôi file từ trong resources ra
            try (InputStream input = Main.class.getClassLoader().getResourceAsStream("application.properties")) {
                if (input != null) {
                    props.load(input);
                    port = Integer.parseInt(props.getProperty("server.port", "8080"));
                    System.out.println("[OK] Da nap config. Port: " + port);
                } else {
                    System.out.println("[WARNING] Khong tim thay application.properties, dung port mac dinh: 8080");
                }
            }

            // -------------------------------------------------------------
            // -------------------------------------------------------------
            // BƯỚC 2: KHỞI TẠO CÁC DAO VÀ SERVICE LÕI
            // -------------------------------------------------------------
            System.out.print("[2/4] Khoi tao cac DAO va Service... ");

            // 1. Đẻ ra mấy thằng DAO trước (bọn này chọc thẳng xuống DB)
            AutoBidDAO autoBidDAO = new AutoBidDAO();
            AuctionDAO auctionDAO = new AuctionDAO();
            UserDAO userDAO       = new UserDAO();


            UserService userService       = UserService.getInstance();
            AuctionService auctionService = AuctionService.getInstance();

            // 3. Khởi tạo AutoBidService với chóp bu 4 tham số y như m chụp
            AutoBidService autoBidService = new AutoBidService(
                    autoBidDAO,
                    auctionService,
                    auctionDAO,
                    userDAO
            );

            System.out.println("[OK]");

            // -------------------------------------------------------------
            // BƯỚC 3: KHỞI TẠO VÀ BẬT ENGINE THỜI GIAN
            // -------------------------------------------------------------
            System.out.print("[3/4] Khoi dong Auction Engine... ");

            // Nếu constructor của Engine m có truyền Service vào thì nhét vào nhé,
            // ở đây tao đang giả định gọi constructor rỗng
            AuctionEngine engine = AuctionEngine.getInstance();
            engine.startEngine();

            System.out.println("[OK] Engine dang quet thoi gian.");

            // -------------------------------------------------------------
            // BƯỚC 4: LẮP RÁP SERVER VÀ ĐÓN KHÁCH
            // -------------------------------------------------------------
            System.out.println("[4/4] Lap rap Auction Server va mo cong... [OK]\n");

            AuctionServer server = new AuctionServer(
                    port,
                    engine,
                    auctionService,
                    autoBidService,
                    userService
            );

            System.out.println(">>> SERVER DA SAN SANG DONG KHACH TAI PORT " + port + ". AN CTRL+C DE TAT. <<<");

            server.start();

        } catch (Exception e) {
            System.err.println("\n[FATAL ERROR] HE THONG KHOI DONG THAT BAI!");
            e.printStackTrace();
            System.exit(1);
        }
    }
}