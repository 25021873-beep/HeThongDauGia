package com.auction.core.network;

import com.auction.core.engine.AuctionEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private final AuctionEngine engine;

    public ClientHandler(Socket socket, AuctionEngine engine) {
        this.clientSocket = socket;
        this.engine = engine;
    }

    @Override
    public void run() {
        try {
            // "Đường ống" để ĐỌC tin nhắn Client gửi tới
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            // "Đường ống" để GỬI tin nhắn trả lại Client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Gửi lời chào ngay khi Client vừa kết nối
            out.println("✅ Kết nối Server Đấu Giá thành công!");

            String clientMessage;
            // Liên tục lắng nghe tin nhắn từ Client này
            while ((clientMessage = in.readLine()) != null) {
                System.out.println("📩 [CLIENT GỬI]: " + clientMessage);

                // TODO: Sau này bạn sẽ viết logic phân tích tin nhắn ở đây.
                // Ví dụ: Nếu clientMessage là "BID:1000", bạn sẽ gọi engine để xử lý tiền.

                // Phản hồi lại cho Client biết đã nhận được
                out.println("Server đã nhận được lệnh: " + clientMessage);
            }

        } catch (IOException e) {
            System.out.println("⚠️ [SERVER] Một Client đã ngắt kết nối đột ngột.");
        } finally {
            try {
                clientSocket.close(); // Dọn dẹp giải phóng bộ nhớ khi khách rời đi
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}