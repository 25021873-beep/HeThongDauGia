package org.example;

import org.example.dao.AuctionDAO;
import org.example.dao.AutoBidDAO;
import org.example.dao.user.UserDAO;
import org.example.network.AuctionServer;
import org.example.service.AuctionEngine;
import org.example.service.AuctionService;
import org.example.service.AutoBidService;
import org.example.service.UserService;
import org.example.utils.ConfigManager;

public class ServerMain {
    public static void main(String[] args) {
        ConfigManager config = ConfigManager.getInstance();
        int port = config.getInt("server.port", 8888);

        AuctionEngine engine = AuctionEngine.getInstance();
        AuctionService auctionService = AuctionService.getInstance();
        UserService userService = UserService.getInstance();
        AutoBidService autoBidService = new AutoBidService(
                new AutoBidDAO(),
                auctionService,
                new AuctionDAO(),
                new UserDAO());

        auctionService.setEngine(engine);
        auctionService.setAutoBidService(autoBidService);
        engine.setAuctionService(auctionService);
        engine.startEngine();

        new AuctionServer(port, engine, auctionService, autoBidService, userService).start();
    }
}
