package org.example.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.example.dto.response.AuctionExtendedResponse;
import org.example.dto.response.AuctionResultResponse;
import org.example.dto.response.AuctionStartedResponse;
import org.example.dto.response.BaseResponse;
import org.example.dto.response.BidUpdateResponse;
import org.example.dto.response.SimpleResponse;
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

public class RobustClientHandler extends ClientHandler {
    private final Socket clientSocket;
    private final AuctionEngine engine;
    private final UserService userService;
    private final AuctionService auctionService;
    private final AutoBidService autoBidService;
    private final Gson gson;

    private volatile PrintWriter out;

    public RobustClientHandler(Socket socket, AuctionEngine engine,
                               UserService userService, AuctionService auctionService,
                               AutoBidService autoBidService) {
        super(socket, engine, userService, auctionService, autoBidService);
        this.clientSocket = socket;
        this.engine = engine;
        this.userService = userService;
        this.auctionService = auctionService;
        this.autoBidService = autoBidService;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class,
                        (JsonDeserializer<LocalDateTime>) (json, type, ctx) ->
                                LocalDateTime.parse(json.getAsString(),
                                        DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .create();
    }

    @Override
    public void run() {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream()))) {

            out = new PrintWriter(clientSocket.getOutputStream(), true);

            SessionContext session = new SessionContext(out, gson);
            AuthController auth = new AuthController(session, userService, gson);
            AuctionController auction = new AuctionController(session, engine, auctionService, this, gson);
            BidController bid = new BidController(session, engine, auctionService, this, gson);
            UserController user = new UserController(session, userService, gson);
            ItemController itemCtrl = new ItemController(session, ItemService.getInstance(), gson);
            bid.setAutoBidService(autoBidService);

            CommandRouter router = new CommandRouter(session, auth, auction, bid, user, itemCtrl);
            session.send(SimpleResponse.success("Ket noi Server thanh cong"));

            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                try {
                    JsonObject json = JsonParser.parseString(line).getAsJsonObject();
                    if (!json.has("command")) {
                        session.send(SimpleResponse.error("JSON thieu truong 'command'"));
                        continue;
                    }

                    String command = json.get("command").getAsString().trim().toUpperCase();
                    boolean shouldDisconnect = router.dispatch(command, json);
                    if (shouldDisconnect) return;

                } catch (JsonSyntaxException | IllegalStateException e) {
                    session.send(SimpleResponse.error("Dinh dang JSON khong hop le"));
                } catch (Exception e) {
                    session.send(SimpleResponse.error(e.getMessage()));
                    System.err.println("[NETWORK] Loi xu ly request: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.err.println("[NETWORK] Client ngat ket noi: " + e.getMessage());
        } finally {
            cleanUp();
        }
    }

    public void send(BaseResponse response) {
        if (out != null) out.println(gson.toJson(response));
    }

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

    @Override
    public void onAuctionExtended(int auctionId, LocalDateTime newEndTime, int addedSeconds) {
        send(new AuctionExtendedResponse(auctionId, addedSeconds, newEndTime));
    }

    @Override
    public void onAuctionStarted(int auctionId, String auctionName) {
        send(new AuctionStartedResponse(auctionId, auctionName, LocalDateTime.now()));
    }

    private void cleanUp() {
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
}
