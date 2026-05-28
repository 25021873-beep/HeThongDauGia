package org.example.network;

import com.google.gson.JsonObject;
import org.example.dto.response.SimpleResponse;
import org.example.network.controller.*;

/**
 * Điều hướng command đến đúng controller.
 */
public class CommandRouter {

    private final SessionContext     session;
    private final AuthController     authController;
    private final AuctionController  auctionController;
    private final BidController      bidController;
    private final UserController     userController;
    private final ItemController     itemController; // Đã thêm ItemController

    // Giữ lại 1 Constructor đầy đủ nhất
    public CommandRouter(SessionContext session,
                         AuthController authController,
                         AuctionController auctionController,
                         BidController bidController,
                         UserController userController,
                         ItemController itemController) {
        this.session           = session;
        this.authController    = authController;
        this.auctionController = auctionController;
        this.bidController     = bidController;
        this.userController    = userController;
        this.itemController    = itemController;
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

            case "TOP_UP":
                userController.handleTopUp(json);
                return false;

            // ── NHÓM LỆNH QUẢN LÝ SẢN PHẨM & PHIÊN ĐẤU GIÁ (DÀNH CHO SELLER/ADMIN) ──
            case "GET_SELLER_ITEMS":
                itemController.handleGetSellerItems();
                return false;

            case "POST_ITEM":
                itemController.handlePostItem(json);
                return false;

            case "DELETE_ITEM":
                itemController.handleDeleteItem(json);
                return false;


            // ── NHÓM LỆNH XEM THÔNG TIN (DÀNH CHO MỌI USER) ──
            case "GET_ALL_AUCTIONS":
                auctionController.handleGetAllAuctions();
                return false;

            case "GET_AUCTION_DETAIL":
                auctionController.handleGetAuctionDetail(json);
                return false;

            case "GET_BID_HISTORY":
                auctionController.handleGetBidHistory(json);
                return false;

            case "GET_USER_BID_HISTORY":
                bidController.handleGetUserBidHistory();
                return false;

            // ── NHÓM LỆNH TƯƠNG TÁC ĐẤU GIÁ (REAL-TIME) ──
            case "JOIN":
                auctionController.handleJoin(json);
                return false;

            case "BID":
                bidController.handleBid(json);
                return false;

            case "SET_AUTO_BID":
                bidController.handleAutoBid(json);
                return false;

            default:
                session.send(SimpleResponse.error("Lenh khong hop le: " + command));
                return false;
        }
    }
}