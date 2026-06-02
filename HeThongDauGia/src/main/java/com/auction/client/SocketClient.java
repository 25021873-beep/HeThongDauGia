package com.auction.client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {

    private static final String HOST = "172.16.66.216";
    private static final int PORT = 8888;

    private static SocketClient instance;

    private SocketClient() {
    }

    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<Message> onMessageReceived;

    public void setOnMessageReceived(Consumer<Message> callback) {
        this.onMessageReceived = callback;
    }

    public void connect() throws IOException {
        socket = new Socket(HOST, PORT);
        out = new PrintWriter(socket.getOutputStream(), true);
        in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        System.out.println("[CLIENT] Đã kết nối server cổng " + PORT);

        Thread listener = new Thread(this::listenFromServer);
        listener.setDaemon(true);
        listener.start();
    }

    private void listenFromServer() {
        try {
            String raw;
            while ((raw = in.readLine()) != null) {
                Message msg = Message.fromJson(raw);
                System.out.println("[CLIENT] Nhận: " + msg.getType());
                if (onMessageReceived != null) {
                    final Message m = msg;
                    onMessageReceived.accept(m);
                }
            }
        } catch (IOException e) {
            System.out.println("[CLIENT] Mất kết nối server");
        }
    }

    private void send(Message msg) {
        if (out != null) {
            out.println(msg.toJson());
            System.out.println("[CLIENT] Gửi: " + msg.getType());
        }
    }

    // Các hàm Frontend gọi
    public void login(String username, String password) {
        String payload = "{\"username\":\"" + username
                + "\",\"password\":\"" + password + "\"}";
        send(new Message(Message.LOGIN, payload, username));
    }

    public void joinAuction(String auctionId) {
        send(new Message(Message.JOIN_AUCTION, auctionId, "client"));
    }

    public void placeBid(String auctionId, double amount) {
        String payload = "{\"auctionId\":\"" + auctionId
                + "\",\"amount\":" + amount + "}";
        send(new Message(Message.PLACE_BID, payload, "client"));
    }

    public void getAllAuctions() {
        send(new Message(Message.GET_AUCTIONS, "", "client"));
    }

    public void logout() {
        send(new Message("LOGOUT", "", "client"));
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}