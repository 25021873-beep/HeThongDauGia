package org.example.network;

import org.example.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final Socket clientSocket;
    private BufferedReader in;
    private PrintWriter out;
    private String clientId;
    private String watchingAuctionId;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.clientId = "unknown_" + clientSocket.getInetAddress();
    }

    @Override
    public void run() {
        try {
            in  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(clientSocket.getOutputStream(), true);

            System.out.println("Handler khởi động cho: " + clientSocket.getInetAddress());

            String raw;
            while ((raw = in.readLine()) != null) {
                try {
                    Message msg = Message.fromJson(raw);
                    handleMessage(msg);
                } catch (Exception e) {
                    System.err.println("Tin nhắn lỗi định dạng từ " + clientId + ": " + raw);
                    sendMessage(new Message(Message.ERROR, "Định dạng tin nhắn không hợp lệ", "SERVER"));
                }
            }

        } catch (IOException e) {
            System.out.println("Cilent ngắt kết nối: " + clientId);
        } finally {
            AuctionServer.removeCilent(this);
            closeConnection();
        }
    }

    private void handleMessage(Message msg) {
        System.out.println("[" + msg.getType() + "] từ " + clientId + " | " + msg.getPayload());

        switch (msg.getType()) {
            case Message.LOGIN:
                handleLogin(msg);
                break;
            case Message.JOIN_AUCTION:
                handleJoinAuction(msg);
                break;
            case Message.PLACE_BID:
                handlePlaceBid(msg);
                break;
            default:
                sendMessage(new Message(Message.ERROR, "Loại tin nhắn không xác định", "SERVER"));
        }
    }

    private void handleLogin(Message msg) {
        // TODO: Backend thêm UserService.authenticate() vào đây
        String username = extractField(msg.getPayload(), "username");

        if (username == null || username.isEmpty()) {
            sendMessage(new Message(Message.LOGIN_FAIL, "Thiếu thông tin đăng nhập", "SERVER"));
            return;
        }

        this.clientId = username;
        System.out.println("Đăng nhập thành công: " + clientId);
        sendMessage(new Message(Message.LOGIN_OK, "Xin chào " + clientId, "SERVER"));
    }

    private void handleJoinAuction(Message msg) {
        String auctionId = msg.getPayload();

        if (auctionId == null || auctionId.isEmpty()) {
            sendMessage(new Message(Message.ERROR, "AuctionId không hợp lệ", "SERVER"));
            return;
        }

        this.watchingAuctionId = auctionId;
        System.out.println(clientId + " vào xem phiên: " + auctionId);
        sendMessage(new Message(Message.JOIN_AUCTION, "Đã vào phòng: " + auctionId, "SERVER"));
    }

    private void handlePlaceBid(Message msg) {
        // TODO: Backend thêm BidService.placeBid() vào đây
        String auctionId = extractField(msg.getPayload(), "auctionId");

        if (auctionId == null || auctionId.isEmpty()) {
            sendMessage(new Message(Message.BID_REJECTED, "AuctionId không hợp lệ", "SERVER"));
            return;
        }

        if (!auctionId.equals(watchingAuctionId)) {
            sendMessage(new Message(Message.BID_REJECTED, "Bạn chưa vào phòng đấu giá này", "SERVER"));
            return;
        }

        System.out.println(clientId + " đặt giá trong phiên: " + auctionId);
        AuctionServer.broadcastToAuction(auctionId, new Message(Message.BID_UPDATE, msg.getPayload(), clientId));
    }

    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.toJson());
        }
    }

    private void closeConnection() {
        try {
            if (in != null)           in.close();
            if (out != null)          out.close();
            if (!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }

    private String extractField(String json, String fieldName) {
        if (json == null) return null;
        String key = "\"" + fieldName + "\":\"";
        int start = json.indexOf(key);
        if (start == -1) return null;
        start += key.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    public String getClientId()          { return clientId; }
    public String getWatchingAuctionId() { return watchingAuctionId; }
}