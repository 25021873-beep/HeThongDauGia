package com.auction.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import org.example.utils.ConfigManager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Singleton quản lý kết nối Socket persistent tới Server.
 *
 * - Giữ socket mở liên tục sau khi LOGIN thành công
 * - Thread daemon lắng nghe mọi message từ server (push + response)
 * - Hỗ trợ sendAndWait() cho request-response đồng bộ
 * - Hỗ trợ setOnPush() cho realtime push (BID_UPDATE, AUCTION_END, ...)
 */
public class ConnectionManager {

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int WAIT_TIMEOUT_SECONDS = 10;
    private static final Gson GSON = new Gson();

    private static ConnectionManager instance;
    private static final String DEFAULT_HOST =
            ConfigManager.getInstance().getString("client.server.host", "104.214.169.224");
    private static final int DEFAULT_PORT =
            ConfigManager.getInstance().getInt("client.server.port", 8888);
    private static final int CONNECT_TIMEOUT = 5000; // 5 giây
    private static final int RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 1000; // 1 giây

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean connected = false;

    // Lưu thông tin user sau khi login thành công
    private int userId;
    private String username;
    private String role;
    private double balance;

    // Callback cho push messages (BID_UPDATE, AUCTION_END, AUCTION_EXTENDED, AUCTION_STARTED)
    private Consumer<JsonObject> onPushMessage;

    // Queue chứa các CompletableFuture đang chờ response
    private final ConcurrentLinkedQueue<CompletableFuture<JsonObject>> pendingRequests = new ConcurrentLinkedQueue<>();

    private ConnectionManager() {}

    public static synchronized ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    // ── Kết nối ──────────────────────────────────────────────────────────────

    /**
     * Kết nối tới server và đọc welcome message.
     * Bắt đầu thread lắng nghe liên tục.
     */
    public void connect(String host, int port) throws IOException {
        if (connected) return;

        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);

        out = new PrintWriter(
                new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true);
        in = new BufferedReader(
                new InputStreamReader(socket.getInputStream(), "UTF-8"));

        // Đọc welcome message từ server
        String welcome = in.readLine();
        System.out.println("[CLIENT] Server welcome: " + welcome);

        connected = true;

        // Khởi động thread lắng nghe
        Thread listener = new Thread(this::listenLoop, "ServerListener");
        listener.setDaemon(true);
        listener.start();

        System.out.println("[CLIENT] Da ket noi server " + host + ":" + port);
    }

    public void connectDefault() throws IOException {
        connect(DEFAULT_HOST, DEFAULT_PORT);
    }

    public static String getDefaultEndpoint() {
        return DEFAULT_HOST + ":" + DEFAULT_PORT;
    }

    // ── Vòng lặp lắng nghe ──────────────────────────────────────────────────

    private void listenLoop() {
        try {
            String line;
            while (connected && (line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                System.out.println("[CLIENT] Nhan: " + line);
                JsonObject json = JsonParser.parseString(line).getAsJsonObject();

                String status = json.has("status") ? json.get("status").getAsString() : "";

                // Phân biệt: push message vs response cho request đang chờ
                if (isPushMessage(status)) {
                    // Push message → gọi callback trên JavaFX thread
                    if (onPushMessage != null) {
                        final JsonObject msg = json;
                        Platform.runLater(() -> onPushMessage.accept(msg));
                    }
                } else {
                    // Response cho request → complete future đang chờ
                    CompletableFuture<JsonObject> future = pendingRequests.poll();
                    if (future != null) {
                        future.complete(json);
                    }
                }
            }
        } catch (IOException e) {
            if (connected) {
                System.err.println("[CLIENT] Mat ket noi server: " + e.getMessage());
            }
        } finally {
            connected = false;
        }
    }

    /**
     * Kiểm tra xem message có phải là push (server tự gửi) hay response cho request.
     * Push messages có status đặc biệt mà server broadcast tới tất cả clients trong room.
     */
    private boolean isPushMessage(String status) {
        return "UPDATE".equals(status)
                || "AUCTION_END".equals(status)
                || "AUCTION_EXTENDED".equals(status)
                || "AUCTION_STARTED".equals(status);
    }

    // ── Gửi request ─────────────────────────────────────────────────────────

    /**
     * Gửi request và đợi response đồng bộ (blocking, có timeout).
     * Dùng cho LOGIN, REGISTER, GET_ALL_AUCTIONS, JOIN, BID, ...
     *
     * @return JsonObject response từ server
     * @throws IOException nếu timeout hoặc lỗi kết nối
     */
    public JsonObject sendAndWait(JsonObject request) throws IOException {
        if (!connected || out == null) {
            throw new IOException("Chua ket noi server");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        pendingRequests.add(future);

        String json = GSON.toJson(request);
        System.out.println("[CLIENT] Gui: " + hidePassword(json));
        out.println(json);

        try {
            return future.get(WAIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            pendingRequests.remove(future);
            throw new IOException("Server khong phan hoi sau " + WAIT_TIMEOUT_SECONDS + "s", e);
        }
    }

    /**
     * Gửi request mà không cần đợi response (fire-and-forget).
     * Dùng cho LOGOUT.
     */
    public void sendOnly(JsonObject request) {
        if (connected && out != null) {
            String json = GSON.toJson(request);
            System.out.println("[CLIENT] Gui (no-wait): " + json);
            out.println(json);
        }
    }

    // ── Push callback ────────────────────────────────────────────────────────

    /**
     * Đăng ký callback nhận push messages từ server.
     * Callback sẽ được gọi trên JavaFX Application Thread.
     */
    public void setOnPushMessage(Consumer<JsonObject> callback) {
        this.onPushMessage = callback;
    }

    /**
     * Xóa push callback (khi rời màn hình AuctionDetail).
     */
    public void clearPushCallback() {
        this.onPushMessage = null;
    }

    // ── Ngắt kết nối ─────────────────────────────────────────────────────────

    /**
     * Gửi LOGOUT và đóng socket.
     */
    public void disconnect() {
        if (connected) {
            try {
                JsonObject logout = new JsonObject();
                logout.addProperty("command", "LOGOUT");
                sendOnly(logout);
            } catch (Exception ignored) {}
        }

        connected = false;
        clearPushCallback();
        pendingRequests.clear();
        userId = 0;
        username = null;
        role = null;
        balance = 0.0;

        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("[CLIENT] Loi dong socket: " + e.getMessage());
        }

        System.out.println("[CLIENT] Da ngat ket noi");
    }

    // ── User info (lưu sau khi login thành công) ─────────────────────────────

    public int getUserId()       { return userId; }
    public String getUsername()  { return username; }
    public String getRole()      { return role; }
    public double getBalance()   { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public boolean isConnected() { return connected; }

    public void setUserInfo(int userId, String username, String role, double balance) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.balance = balance;
    }

    // ── Helper ───────────────────────────────────────────────────────────────

    private String hidePassword(String json) {
        return json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
    }
}
