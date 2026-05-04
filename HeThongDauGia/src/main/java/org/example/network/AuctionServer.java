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
    //Cổng 9999
    private static final int PORT = 9999;
    //Số nhân viên tối đa là 50, 51 khách thì người cuối sẽ phải chờ cho 1 người thoát
    private static final int MAX_THREADS = 50;
//Clients: dang sách các khacsh đang ket noi
    private static final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
// Hồ bơi gồm 50 thread có sẵn
    private static final ExecutorService pool = Executors.newFixedThreadPool(MAX_THREADS);

    public static void main(String[] args) {
        System.out.println("Máy chủ đấu giá đang khởi động tại cổng " + PORT);

//Mở cổng kết nối
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server sẵn sàng nhận kết nối");

//Vòng lặp vô hạn hoạt động nhận khách 24/7
            while (true) {
                //Máy chủ đứng hình, chowf 1 app client kết nối mạng thành cong, hàm này sẽ nhả ra clientSocket đầu dây mạng nối trujwc tiếp đến khách
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client mới kết nối: " + clientSocket.getInetAddress());

//Phân chia nhiệm vụ, giao từng thread cho nhân viên
                ClientHandler handler = new ClientHandler(clientSocket);
                //Ghi tên nhân viên vào danh sách clients
                clients.add(handler);
                //Cho nhân viên vào hồ bơi để có thể hoạt động độc lập song song
                pool.execute(handler);
            }

        } catch (IOException e) {
            System.err.println("Lỗi server: " + e.getMessage());
        }
    }
    //Thông báo cho từng client, dùng vongf for để tìm từng nhân viên ửi từng tin nhắn cho khách
    public static void broadcastToAll(Message msg) {
        System.out.println("Broadcast tất cả [" + clients.size() + " client]: " + msg.getType());
        for (ClientHandler client : clients) {
            client.sendMessage(msg);
        }
    }
//Tương tự như trên nhưng cần đúng phòng thì mới có thể guiwr tin
    public static void broadcastToAuction(String auctionId, Message msg) {
        int count = 0;
        for (ClientHandler client : clients) {
            if (auctionId.equals(client.getWatchingAuctionId())) {
                client.sendMessage(msg);
                count++;
            }
        }
        System.out.println("Broadcast phiên [" + auctionId + "] đến " + count + " client");
    }
//Hàm xóa client

    public static void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Đã xóa client: " + client.getClientId()
                + " | Còn lại: " + clients.size() + " client");
    }

    public static int getClientCount() {
        return clients.size();
    }
}