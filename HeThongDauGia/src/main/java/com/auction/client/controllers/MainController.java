package com.auction.client.controllers;

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

    @FXML private Label lblUserInfo;
    @FXML private StackPane contentArea;
    @FXML private StackPane rootPane;
    @FXML private ImageView bgTexture;
    @FXML private ImageView bgPattern;

    //nut sidebar
    @FXML private Button btnAuctionList, btnBidHistory, btnProductMgmt, btnUserMgmt;

    private static final String ACTIVE_STYLE = "-fx-background-color: #F57D1F; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;";
    private static final String INACTIVE_STYLE = "-fx-background-color: transparent; -fx-text-fill: #CCCCCC; -fx-font-size: 14px; -fx-cursor: hand; -fx-background-radius: 8;";

    @FXML
    public void initialize() {
        //an tat ca cac nut
        setButtonVisible(btnAuctionList, false);
        setButtonVisible(btnBidHistory, false);
        setButtonVisible(btnProductMgmt, false);
        setButtonVisible(btnUserMgmt, false);

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
        lblUserInfo.setText("Xin chào, " + role);

        //bat cac menu tuong ung role
        if ("Bidder".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnBidHistory, true);
            handleShowAuctionList(); //mac dinh dsach dau gia cho bidder

        } else if ("Seller".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnProductMgmt, true);
            handleShowProductMgmt(); // mac dinh mh qly spham cho seller

        } else if ("Admin".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnUserMgmt, true);
            handleShowUserMgmt(); // mac dinh qly user cho admin
        }
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
        Button[] allButtons = {btnAuctionList, btnBidHistory, btnProductMgmt, btnUserMgmt};
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
    private void handleLogout(ActionEvent event) {
        try {
            // Nạp lại màn hình Login
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle("Hệ thống Đấu giá trực tuyến - Đăng nhập");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();

            System.out.println("Đăng xuất thành công!");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi đăng xuất: " + e.getMessage());
        }
    }
}