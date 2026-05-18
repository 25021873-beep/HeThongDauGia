package org.example.network;

import com.google.gson.*;
import org.example.dto.response.AuctionResultResponse;
import org.example.dto.response.BaseResponse;
import org.example.dto.response.BidUpdateResponse;
import org.example.dto.response.SimpleResponse;
import org.example.network.controller.AuctionController;
import org.example.network.controller.AuthController;
import org.example.network.controller.BidController;
import org.example.network.controller.UserController;
import org.example.observer.BidObserver;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
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

    private volatile PrintWriter out;
    private final Gson           gson;

    public ClientHandler(Socket socket, AuctionEngine engine,
                         UserService userService, AuctionService auctionService) {
        this.clientSocket   = socket;
        this.engine         = engine;
        this.userService    = userService;
        this.auctionService = auctionService;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                                LocalDateTime.parse(json.getAsString(),
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
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

    // ── Gửi response ──────────────────────────────────────────────────────────

    public void send(BaseResponse response) {
        if (out != null) out.println(gson.toJson(response));
    }

    // ── Vòng lặp chính ────────────────────────────────────────────────────────

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            SessionContext   session  = new SessionContext(out, gson);
            AuthController   auth     = new AuthController(session, userService, gson);
            AuctionController auction  = new AuctionController(session, engine, this, gson);
            BidController    bid      = new BidController(session, engine, auctionService, this, gson);
            UserController   user     = new UserController(session, userService, gson);
            CommandRouter    router   = new CommandRouter(session, auth, auction, bid, user);

            session.send(SimpleResponse.success("Ket noi Server thanh cong"));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                try {
                    JsonObject json    = JsonParser.parseString(line).getAsJsonObject();
                    if (!json.has("command")) {
                        session.send(SimpleResponse.error("JSON thieu truong 'command'"));
                        continue;
                    }
                    String command = json.get("command").getAsString().trim().toUpperCase();
                    boolean shouldDisconnect = router.dispatch(command, json);
                    if (shouldDisconnect) return;
                } catch (JsonSyntaxException | IllegalStateException e) {
                    session.send(SimpleResponse.error("Dinh dang JSON khong hop le"));
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
        // THAY ĐỔI: Gọi xóa observer từ RoomManager thay vì từ từng Auction
        if (engine != null && engine.getRoomManager() != null) {
            engine.getRoomManager().clearObserverFromAllRooms(this);
        }

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
}