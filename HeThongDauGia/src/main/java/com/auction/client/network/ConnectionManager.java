package com.auction.client.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.function.Consumer;

public class ConnectionManager {
    private static final String HOST = "26.139.15.134";
    private static final int PORT = 8888;
    private static final int CONNECT_TIMEOUT = 5000; // 5 giây
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 1000; // 1 giây

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Consumer<String> onMessageReceived;

    public void setOnMessageReceived(Consumer<String> callback) {
        this.onMessageReceived = callback;
    }

    public void connect() throws IOException {
        IOException lastException = null;

        for (int attempt = 1; attempt <= RETRY_COUNT; attempt++) {
            try {
                System.out.println("[CLIENT] Kết nối lần " + attempt + "/" + RETRY_COUNT + "...");
                socket = new Socket();
                socket.connect(new java.net.InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT);

                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println("[CLIENT] ✓ Đã kết nối server " + HOST + ":" + PORT);

                Thread listener = new Thread(this::listenFromServer);
                listener.setDaemon(true);
                listener.start();
                return;

            } catch (SocketTimeoutException e) {
                lastException = e;
                System.out.println("[CLIENT] ✗ Timeout lần " + attempt);
                if (attempt < RETRY_COUNT) {
                    try { Thread.sleep(RETRY_DELAY); } catch (InterruptedException ignored) {}
                }
            } catch (IOException e) {
                lastException = e;
                System.out.println("[CLIENT] ✗ Lỗi lần " + attempt + ": " + e.getMessage());
                if (attempt < RETRY_COUNT) {
                    try { Thread.sleep(RETRY_DELAY); } catch (InterruptedException ignored) {}
                }
            }
        }

        throw lastException != null ? lastException : new IOException("Không thể kết nối sau " + RETRY_COUNT + " lần thử");
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

    public void send(String text) {
        if (out != null) {
            out.println(text);
            System.out.println("[CLIENT] Gửi: " + text);
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }
}
