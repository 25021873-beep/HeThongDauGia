package org.example.Client;

import org.example.model.Message;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final String userId;

    // Frontend đăng ký hàm này để nhận tin nhắn từ server
    //Chuông báo tại giao diện, có tin từ server chuoong sẽ reo
    private Consumer<Message> onMessageReceived;

    public SocketClient(String userId) {
        this.userId = userId;
    }

    public void setOnMessageReceived(Consumer<Message> callback) {
        this.onMessageReceived = callback;
    }

    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("Đã kết nối đến server");

        // Thread riêng lắng nghe tin từ server liên tục
        Thread listener = new Thread(this::listenFromServer);
        listener.setDaemon(true);
        listener.start();
    }

    private void listenFromServer() {
        try {
            String raw;
            //Loong này liên tục đọc tin nhắn từ ống nghe
            //Khi có một chuỗi đến, sẽ chuyển thành obj và bấm cái chuông để gửi về giao diện
            while ((raw = in.readLine()) != null) {
                Message msg = Message.fromJson(raw);
                System.out.println("Nhận từ server: " + msg.getType());
                if (onMessageReceived != null) {
                    onMessageReceived.accept(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("Mất kết nối server");
        }
    }

    public void send(Message msg) {
        if (out != null) out.println(msg.toJson());
    }

    // Các hàm tiện ích cho Frontend gọi
    public void login(String username, String password) {
        String payload = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        send(new Message(Message.LOGIN, payload, userId));
    }

    public void joinAuction(String auctionId) {
        send(new Message(Message.JOIN_AUCTION, auctionId, userId));
    }

    public void placeBid(String auctionId, double amount) {
        String payload = "{\"auctionId\":\"" + auctionId + "\",\"amount\":" + amount + "}";
        send(new Message(Message.PLACE_BID, payload, userId));
    }

    public void disconnect() {
        try { if (socket != null) socket.close(); }
        catch (IOException ignored) {}
    }
}