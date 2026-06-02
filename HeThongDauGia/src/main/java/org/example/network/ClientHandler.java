package org.example.network;

import com.google.gson.*;
import org.example.dto.response.*;
import org.example.network.controller.AuctionController;
import org.example.network.controller.AuthController;
import org.example.network.controller.BidController;
import org.example.network.controller.ItemController;
import org.example.network.controller.UserController;
import org.example.observer.BidObserver;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;
import org.example.service.ItemService;
import org.example.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ClientHandler implements Runnable, BidObserver {

    private final Socket         clientSocket;
    private final AuctionEngine  engine;
    private final UserService    userService;
    private final AuctionService auctionService;
    private final AutoBidService autoBidService;

    private volatile PrintWriter out;
    private final Gson           gson;

    public ClientHandler(Socket socket, AuctionEngine engine,
                         UserService userService, AuctionService auctionService,
                         AutoBidService autoBidService) {
        this.clientSocket   = socket;
        this.engine         = engine;
        this.userService    = userService;
        this.auctionService = auctionService;
        this.autoBidService = autoBidService;

        // Cấu hình Gson để xử lý định dạng thời gian thực (Cả Đọc và Ghi)
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (com.google.gson.JsonSerializer<LocalDateTime>) (src, type, ctx) ->
                                new com.google.gson.JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                                LocalDateTime.parse(json.getAsString(),
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
    }

        /**
         * Backwards-compatible constructor for tests that do not need AutoBidService injected.
         */
        public ClientHandler(Socket socket, AuctionEngine engine,
                 UserService userService, AuctionService auctionService) {
        this(socket, engine, userService, auctionService,
            new AutoBidService(new org.example.dao.AutoBidDAO(), auctionService,
                new org.example.dao.AuctionDAO(), new org.example.dao.user.UserDAO()));
        }

    // ── BidObserver implementation ────────────────────────────────────────────

    @Override
    public void onBidPlaced(int auctionId, String bidderUsername, BigDecimal newPrice) {
        send(new BidUpdateResponse(auctionId, bidderUsername, newPrice));
    }

    @Override
    public void onAuctionEnded(int auctionId, String auctionName,
                               String winnerUsername, BigDecimal finalPrice) {
        send(new AuctionResultResponse(
                auctionId, auctionName, winnerUsername, finalPrice, LocalDateTime.now()));
    }

    /** Nhận event gia hạn anti-snipe → gửi thời gian mới về client */
    @Override
    public void onAuctionExtended(int auctionId, LocalDateTime newEndTime, int addedSeconds) {
        send(new AuctionExtendedResponse(auctionId, addedSeconds, newEndTime));
    }

    @Override
    public void onAuctionStarted(int auctionId, String auctionName) {
        send(new AuctionStartedResponse(auctionId, auctionName, LocalDateTime.now()));
    }

    // ── Gửi response ─────────────────────────────────────────────────────────

    public void send(BaseResponse response) {
        if (out != null) out.println(gson.toJson(response));
    }

    // ── Vòng lặp chính ───────────────────────────────────────────────────────

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            // 1. Khởi tạo Context và các Controller
            SessionContext    session  = new SessionContext(out, gson);
            AuthController    auth     = new AuthController(session, userService, gson);
            AuctionController auction  = new AuctionController(session, engine, auctionService, this, gson);
            BidController     bid      = new BidController(session, engine, auctionService, this, gson);
            UserController    user     = new UserController(session, userService, gson);
            ItemController    itemCtrl = new ItemController(session, ItemService.getInstance(), gson);

            // 2. Gắn AutoBidService vào BidController (Sử dụng service đã tiêm từ Server, không new mới)
            bid.setAutoBidService(this.autoBidService);

            // 3. Khởi tạo Router với đủ 6 Controller
            CommandRouter     router   = new CommandRouter(session, auth, auction, bid, user, itemCtrl);

            session.send(SimpleResponse.success("Ket noi Server thanh cong"));

            // 4. Vòng lặp lắng nghe lệnh từ Client
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                    if (!json.has("command")) {
                        session.send(SimpleResponse.error("JSON thieu truong 'command'"));
                        continue;
                    }

                    String  command          = json.get("command").getAsString().trim().toUpperCase();
                    boolean shouldDisconnect = router.dispatch(command, json);

                    // Nếu nhận được lệnh LOGOUT (router trả về true), thoát vòng lặp
                    if (shouldDisconnect) return;

                } catch (JsonSyntaxException | IllegalStateException e) {
                    session.send(SimpleResponse.error("Dinh dang JSON khong hop le"));
                } catch (Exception e) { // <--- THÊM TỪ ĐOẠN NÀY
                session.send(SimpleResponse.error(e.getMessage()));
                System.err.println("[NETWORK] Loi xu ly request: " + e.getMessage()
                        + " | Nguyen nhan goc: " + rootCauseMessage(e));
                e.printStackTrace();
            }
            }

        } catch (IOException e) {
            System.err.println("[NETWORK] Client ngat ket noi: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private void cleanUp() {
        // Hủy đăng ký client này khỏi toàn bộ các phòng đấu giá đang xem
        if (engine != null && engine.getRoomManager() != null) {
            engine.getRoomManager().clearObserverFromAllRooms(this);
        }

        // Đóng luồng
        try {
            if (out != null) out.close();
            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
        } catch (IOException e) {
            System.err.println("[NETWORK] Loi khi dong ket noi: " + e.getMessage());
        }
    }

    public String getUsername() {
        return "Client@" + clientSocket.getInetAddress().getHostAddress();
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current.getMessage();
    }
}