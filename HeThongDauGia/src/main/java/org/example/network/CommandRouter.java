package org.example.network;

import com.google.gson.JsonObject;
import org.example.dto.response.SimpleResponse;
import org.example.network.controller.AuctionController;
import org.example.network.controller.AuthController;
import org.example.network.controller.BidController;
import org.example.network.controller.UserController;

/**
 * Điều hướng command đến đúng controller.
 *
 * ClientHandler chỉ cần gọi router.dispatch(command, json) —
 * không cần biết gì về các controller bên trong.
 *
 * Để thêm lệnh mới: tạo controller, thêm 1 case vào switch.
 * Để thêm lệnh LOGOUT: router trả về true → ClientHandler thoát vòng lặp.
 */
public class CommandRouter {

    private final SessionContext     session;
    private final AuthController     authController;
    private final AuctionController  auctionController;
    private final BidController      bidController;
    private final UserController     userController;

    public CommandRouter(SessionContext session,
                         AuthController authController,
                         AuctionController auctionController,
                         BidController bidController,
                         UserController userController) {
        this.session           = session;
        this.authController    = authController;
        this.auctionController = auctionController;
        this.bidController     = bidController;
        this.userController    = userController;
    }

    /**
     * Điều hướng command đến handler tương ứng.
     *
     * @return true nếu client yêu cầu ngắt kết nối (LOGOUT), false nếu tiếp tục.
     */
    public boolean dispatch(String command, JsonObject json) {
        switch (command) {
            case "LOGIN":
                authController.handleLogin(json);
                return false;

            case "REGISTER":
                authController.handleRegister(json);
                return false;

            case "LOGOUT":
                authController.handleLogout();
                return true; // Tín hiệu ClientHandler thoát vòng lặp

            case "CHANGE_PASSWORD":
                authController.handleChangePassword(json);
                return false;

            case "GET_ALL_AUCTIONS":
                auctionController.handleGetAllAuctions();
                return false;

            case "JOIN":
                auctionController.handleJoin(json);
                return false;

            case "BID":
                bidController.handleBid(json);
                return false;

            case "TOP_UP":
                userController.handleTopUp(json);
                return false;

            case "CREATE_AUCTION":
                auctionController.handleCreateAuction(json);
                return false;

            case "SET_AUTO_BID":
                bidController.handleAutoBid(json);
                return false;

            case "GET_BID_HISTORY":
                auctionController.handleGetBidHistory(json);
                return false;

            default:
                session.send(SimpleResponse.error("Lenh khong hop le: " + command));
                return false;

        }
    }
}