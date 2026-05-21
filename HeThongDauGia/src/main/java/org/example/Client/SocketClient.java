package org.example.Client;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class SocketClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;

    public void setOnMessageReceived(Consumer<String> callback) {
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
                System.out.println("[CLIENT] Nhận: " + raw);
                if (onMessageReceived != null) {
                    final String msg = raw;
                    onMessageReceived.accept(msg);
                }
            }
        } catch (IOException e) {
            System.out.println("[CLIENT] Mất kết nối server");
        }
    }

    private void sendRaw(String text) {
        if (out != null) {
            out.println(text);
            System.out.println("[CLIENT] Gửi: " + text);
        }
    }

    // Gửi đúng format Backend yêu cầu
    public void login(String username, String password) {
        sendRaw("LOGIN|" + username + "|" + password);
    }

    public void joinAuction(String auctionId) {
        sendRaw("JOIN|" + auctionId);
    }

    public void placeBid(String auctionId, double amount) {
        sendRaw("BID|" + auctionId + "|" + amount);
    }

    public void getAllAuctions() {
        sendRaw("GET_ALL_AUCTIONS");
    }

    public void logout() {
        sendRaw("LOGOUT");
    }

    public void disconnect() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }
}