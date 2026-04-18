package org.example.network;

import org.example.model.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuctionServer {

    private static final int PORT = 9999;
    private static final int MAX_THREADS = 50;

    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static final ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) {
        System.out.println("Máy chủ đấu giá đang khởi động tại cổng " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server sẵn sàng nhận kết nối...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                clients.add(handler);
                pool.execute(handler);
            }

        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }

    public static void broadcastToAll(Message msg) {
        System.out.println("Broadcast tất cả [" + clients.size() + " client]: " + msg.getType());
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }

    public static void broadcastToAuction(String auctionId, Message msg) {
        int count = 0;
        for (ClientHandler client : clients) {
            if (auctionId.equals(client.getWatchingAuctionId())) {
                client.sendMessage(msg);
                count++;
            }
        }
        System.out.println("Broadcast phiên [" + auctionId + "] đến " + count + " cilent");
    }

    public static void removeCilent(ClientHandler cilent) {
        clients.remove(cilent);
        System.out.println("Đã xóa client: " + cilent.getClientId()
                + " | Còn lại: " + clients.size() + " client");
    }

    public static int getClientCount() {
        return clients.size();
    }
}