package org.example.network;

import org.example.Client.SocketClient;
import org.example.model.Message;

public class TestConnection {
    public static void main(String[] args) throws Exception {

        // Bước 1: Chạy server trong nền
        new Thread(() -> AuctionServer.main(new String[]{})).start();
        Thread.sleep(500); // Chờ server khởi động

        // Bước 2: Tạo 1 client kết nối vào
        SocketClient client = new SocketClient("user1");

        // Bước 3: Đăng ký nhận tin từ server
        client.setOnMessageReceived(msg ->
                System.out.println(">>> Nhận từ server: " + msg.getType() + " | " + msg.getPayload())
        );

        // Bước 4: Kết nối và đăng nhập
        client.connect();
        client.login("alice", "123");
        // Thêm vào TestConnection.java sau dòng login
        SocketClient client2 = new SocketClient("user2");
        client2.setOnMessageReceived(msg ->
                System.out.println(">>> Client2 nhận: " + msg.getType() + " | " + msg.getPayload())
        );
        client2.connect();
        client2.login("bob", "456");

        Thread.sleep(200);

// Cả 2 vào cùng phòng
        client.joinAuction("auction-001");
        client2.joinAuction("auction-001");

        Thread.sleep(200);

// Client1 đặt giá → Client2 có nhận được không?
        client.placeBid("auction-001", 500000);

        Thread.sleep(1000);
        System.out.println("=== Test xong ===");
        System.exit(0);
    }
}
