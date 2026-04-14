package org.example;



import com.auction.core.engine.AuctionEngine;
import com.auction.core.model.Auction;
import com.auction.core.model.Bidder;
import com.auction.core.model.Item;


import java.time.LocalDateTime;

public class Main {
    public static void main(String[] args) {
        AuctionEngine engine = new AuctionEngine();

        // 1. Tạo các đối tượng giả lập
        Item rolexWatch = new Item("Đồng hồ Rolex Cổ");
        Bidder userHung = new Bidder("Hung_deptrai");
        Bidder userAn = new Bidder("An_gia_giau");

        // 2. Mở phiên đấu giá (Kết thúc sau 10 giây)
        LocalDateTime thoiGianKetThuc = LocalDateTime.now().plusSeconds(10);
        Auction phienDauGia1 = new Auction(rolexWatch, 5000.0, thoiGianKetThuc);

        // 3. Đưa vào Động cơ và khởi động
        engine.addAuction(phienDauGia1);
        engine.startEngine();

        // 4. Các người dùng tiến hành đặt giá (Luồng chính vẫn rảnh rỗi)
        phienDauGia1.placeBid(userHung, 5500.0);
        phienDauGia1.placeBid(userAn, 5200.0); // Bị từ chối vì thấp hơn Hùng
        phienDauGia1.placeBid(userAn, 6000.0); // Hợp lệ
    }
}
