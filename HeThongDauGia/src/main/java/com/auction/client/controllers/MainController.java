package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    // Static instance để các controller con truy cập dễ dàng
    private static MainController instance;

    public static MainController getInstance() {
        return instance;
    }

    @FXML private Label lblUserInfo;
    @FXML private Label lblBalance;
    @FXML private StackPane contentArea;
    @FXML private StackPane rootPane;
    @FXML private ImageView bgTexture;
    @FXML private ImageView bgPattern;

    //nut sidebar
    @FXML private Button btnAuctionList, btnBidHistory, btnProductMgmt, btnUserMgmt, btnDeposit;

    private static final String ACTIVE_STYLE = "-fx-background-color: #F57D1F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #CCCCCC; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 8;";

    // ID phiên đấu giá mà user đang xem chi tiết (-1 = không xem phiên nào)
    private volatile int currentViewingAuctionId = -1;

    @FXML
    public void initialize() {
        //an tat ca cac nut
        setButtonVisible(btnAuctionList, false);
        setButtonVisible(btnBidHistory, false);
        setButtonVisible(btnProductMgmt, false);
        setButtonVisible(btnUserMgmt, false);
        setButtonVisible(btnDeposit, false);
        if (lblBalance != null) lblBalance.setVisible(false);

        //bind background images to window size
        if (rootPane != null && bgTexture != null && bgPattern != null) {
            bgTexture.fitWidthProperty().bind(rootPane.widthProperty());
            bgTexture.fitHeightProperty().bind(rootPane.heightProperty());
            bgPattern.fitWidthProperty().bind(rootPane.widthProperty());
            bgPattern.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

//ham dc goi de truyen role sau khi dang nhap
    public void configureSidebar(String role) {
        instance = this; // lưu static instance để controller con truy cập
        lblUserInfo.setText("Xin chào, " + ConnectionManager.getInstance().getUsername());

        //bat cac menu tuong ung role
        if ("BIDDER".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnBidHistory, true);
            setButtonVisible(btnDeposit, true);
            if (lblBalance != null) {
                lblBalance.setVisible(true);
                updateBalanceDisplay();
            }
            handleShowAuctionList(); //mac dinh dsach dau gia cho bidder

        } else if ("SELLER".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnProductMgmt, true);
            handleShowProductMgmt(); // mac dinh mh qly spham cho seller

        } else if ("ADMIN".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnUserMgmt, true);
            handleShowUserMgmt(); // mac dinh qly user cho admin
        }

        // Đăng ký global push handler để nhận thông báo bid từ mọi phiên
        ConnectionManager.getInstance().setGlobalPushHandler(this::handleGlobalPush);
    }

    //ham bat/tat nut
    private void setButtonVisible(Button btn, boolean isVisible) {
        if (btn != null) {
            btn.setVisible(isVisible);
            btn.setManaged(isVisible);
        }
    }

    //ham highlight nut dang active
    private void setActiveButton(Button activeBtn) {
        Button[] allButtons = {btnAuctionList, btnBidHistory, btnProductMgmt, btnUserMgmt, btnDeposit};
        for (Button btn : allButtons) {
            if (btn != null) {
                btn.setStyle(INACTIVE_STYLE);
            }
        }
        if (activeBtn != null) {
            activeBtn.setStyle(ACTIVE_STYLE);
        }
    }

    /**
     * Hoán đổi nội dung vào Center StackPane.
     * Public để các controller con (VD: AuctionDetailController) có thể gọi.
     */
    public void loadContent(String fxmlFileName) {
        // Khi chuyển màn hình, clear trạng thái "đang xem phiên" để toast hoạt động
        clearCurrentViewingAuctionId();
        try {
            // Nạp file giao diện con từ thư mục /fxml/
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxmlFileName));
            Node node = loader.load();

            // Xóa view cũ, thêm view mới
            contentArea.getChildren().clear();
            contentArea.getChildren().add(node);

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi nạp file: " + fxmlFileName);
        }
    }

    /**
     * Lấy StackPane contentArea để các controller con có thể sử dụng.
     */
    public StackPane getContentArea() {
        return contentArea;
    }

    public void updateBalanceDisplay() {
        if (lblBalance != null) {
            double balance = ConnectionManager.getInstance().getBalance();
            lblBalance.setText(String.format("Số dư: %,.0f VND", balance));
        }
    }

    // =========================================================
    // --- QUẢN LÝ CURRENT VIEWING AUCTION ID ---
    // =========================================================

    /**
     * Đặt ID phiên đấu giá đang xem (khi vào AuctionDetail).
     * Push BID_UPDATE cho phiên này sẽ KHÔNG hiện toast.
     */
    public void setCurrentViewingAuctionId(int auctionId) {
        this.currentViewingAuctionId = auctionId;
    }

    /**
     * Xóa ID phiên đang xem (khi rời AuctionDetail).
     */
    public void clearCurrentViewingAuctionId() {
        this.currentViewingAuctionId = -1;
    }

    // =========================================================
    // --- GLOBAL PUSH HANDLER (TOAST NOTIFICATION) ---
    // =========================================================

    /**
     * Xử lý push message toàn cục.
     * Chỉ hiện toast khi user KHÔNG đang xem phiên tương ứng.
     */
    private void handleGlobalPush(JsonObject msg) {
        if (!msg.has("status")) return;
        String status = msg.get("status").getAsString();
        int msgAuctionId = msg.has("auctionId") ? msg.get("auctionId").getAsInt() : -1;

        // Nếu user đang xem đúng phiên này → SKIP (AuctionDetailController đã xử lý UI)
        if (msgAuctionId == currentViewingAuctionId) return;

        switch (status) {
            case "UPDATE": // BID_UPDATE
                String bidder = msg.has("bidderUsername") ? msg.get("bidderUsername").getAsString() : "Ai đó";
                double newPrice = msg.has("newPrice") ? msg.get("newPrice").getAsDouble() : 0;
                String productName = msg.has("auctionName") ? msg.get("auctionName").getAsString()
                        : ("Phiên #" + msgAuctionId);
                ToastManager.showBidNotification(productName, bidder, newPrice);
                
                // Cập nhật giá realtime trên Dashboard card (nếu đang ở Dashboard)
                DashboardController dc = DashboardController.getInstance();
                if (dc != null) {
                    dc.updatePrice(msgAuctionId, newPrice);
                }
                break;

            case "AUCTION_END":
                String winner = msg.has("winnerUsername") ? msg.get("winnerUsername").getAsString() : "Không rõ";
                double finalPrice = msg.has("finalPrice") ? msg.get("finalPrice").getAsDouble() : 0;
                String auctionName = msg.has("auctionName") ? msg.get("auctionName").getAsString()
                        : ("Phiên #" + msgAuctionId);
                ToastManager.showWarning(
                        "⏱ Phiên \"" + auctionName + "\" đã kết thúc!\n"
                        + "Người thắng: " + winner + " | Giá: " + String.format("%,.0f VNĐ", finalPrice));
                break;

            case "AUCTION_EXTENDED":
                String extName = msg.has("auctionName") ? msg.get("auctionName").getAsString()
                        : ("Phiên #" + msgAuctionId);
                ToastManager.showWarning("⏱ Phiên \"" + extName + "\" đã được gia hạn thời gian!");
                break;

            case "AUCTION_STARTED":
                String startName = msg.has("auctionName") ? msg.get("auctionName").getAsString()
                        : ("Phiên #" + msgAuctionId);
                ToastManager.showInfo("🔔 Phiên \"" + startName + "\" đã bắt đầu!");
                break;
        }
    }

    // =========================================================
    // --- CÁC SỰ KIỆN CLICK TỪ SIDEBAR (ĐÃ ĐỒNG BỘ VỚI FXML) ---
    // =========================================================

    @FXML
    private void handleShowAuctionList() {
        setActiveButton(btnAuctionList);
        // Tải màn hình Dashboard của Bidder
        loadContent("Dashboard.fxml");
    }

    @FXML
    private void handleShowBidHistory() {
        setActiveButton(btnBidHistory);
        // Tải màn hình Lịch sử đấu giá của Bidder
        loadContent("BidHistory.fxml");
    }

    @FXML
    private void handleShowProductMgmt() {
        setActiveButton(btnProductMgmt);
        // Tải màn hình Quản lý sản phẩm của Seller
        loadContent("ProductManagement.fxml");
    }

    @FXML
    private void handleShowUserMgmt() {
        setActiveButton(btnUserMgmt);
        // Tải màn hình Quản lý người dùng của Admin
        loadContent("UserManagement.fxml");
    }

    @FXML
    private void handleShowDeposit() {
        setActiveButton(btnDeposit);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Deposit.fxml"));
            Parent root = loader.load();
            
            // Pass this MainController to the DepositController so it can update balance after success
            com.auction.client.controllers.DepositController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle("Nạp tiền vào tài khoản");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi nạp file Deposit.fxml");
        }
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        // 1. Xóa static instance
        instance = null;

        // 2. Gửi command LOGOUT và đóng kết nối (không cần đợi)
        Thread logoutThread = new Thread(() -> {
            ConnectionManager.getInstance().disconnect();
        });
        logoutThread.start();

        // 2. Nạp lại màn hình Login trên JavaFX Thread
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Swap root để giữ nguyên kích thước cửa sổ
            stage.getScene().setRoot(root);
            stage.setTitle("Hệ thống Đấu giá trực tuyến - Đăng nhập");

            System.out.println("Đăng xuất thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi đăng xuất: " + e.getMessage());
        }
    }
}