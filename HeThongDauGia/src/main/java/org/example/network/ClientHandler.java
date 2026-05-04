package org.example.network;

import org.example.model.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
//Đường dây nối mạng trực tiếp đến khách
    private final Socket clientSocket;
//Oong nghe phản hồi của khách
    private BufferedReader in;
 //Phản hồi lại
    private PrintWriter out;
 //Sổ tay lưu truwx xem mình đang phục vụ ai
    private String clientId;
//Kiểm tra xem khách ứng ở phòng naof
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
            //Nhânvienen ngồi canh ống nghe liên tucj
            while ((raw = in.readLine()) != null) {
                try {
                    //Nếu có khách liên lạc bằng chuỗi raw sẽ chuyển chuỗi raw thành nội dung đọc được,rồi ửi cho hàm xử lý tinh nắn
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
            AuctionServer.removeClient(this);
            closeConnection();
        }
    }

    private void handleMessage(Message msg) {
        System.out.println("[" + msg.getType() + "] từ " + clientId + " | " + msg.getPayload());
//Nhận biết dựa trên phong bì thu mà phân loại nhung nhiệm vụ cân làm
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
//Bóc chuỗi lấy tn người dùng
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
//Tìm phòng mà khách muốn vào
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
//Kiểm tra xem khách đặt giá có đúng phòng không
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
//Chỉ cần cho msg vào đây, sẽ tự động chuyển thành chuỗi raw
    public void sendMessage(Message msg) {
        if (out != null) {
            out.println(msg.toJson());
        }
    }
//Đóng kết nối
    private void closeConnection() {
        try {
            if (in != null)           in.close();
            if (out != null)          out.close();
            if (!clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Lỗi đóng kết nối: " + e.getMessage());
        }
    }

//Cắt chuỗi tìm đúng giá trị cần tìm
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