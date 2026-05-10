package com.auction.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML private Label lblUserInfo;
    @FXML private StackPane contentArea;

    // Các nút trên Sidebar
    @FXML private Button btnAuctionList, btnBidHistory, btnProductMgmt, btnUserMgmt;

    @FXML
    public void initialize() {
        // 1. Khởi tạo ban đầu: Ẩn tất cả các nút trước khi nhận Role từ màn Login
        setButtonVisible(btnAuctionList, false);
        setButtonVisible(btnBidHistory, false);
        setButtonVisible(btnProductMgmt, false);
        setButtonVisible(btnUserMgmt, false);
    }

    /**
     * Hàm này được LoginController gọi để truyền Role sang sau khi đăng nhập thành công.
     * Cấu hình ẩn/hiện Sidebar và tự động nạp màn hình mặc định.
     */
    public void configureSidebar(String role) {
        lblUserInfo.setText("Xin chào, " + role);

        // 2. Bật các menu tương ứng và nạp màn hình mặc định bằng các hàm chuẩn xác
        if ("Bidder".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnBidHistory, true);
            handleShowAuctionList(); // Mặc định mở Danh sách đấu giá cho Bidder

        } else if ("Seller".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnProductMgmt, true);
            handleShowProductMgmt(); // Mặc định mở Quản lý sản phẩm cho Seller

        } else if ("Admin".equalsIgnoreCase(role)) {
            setButtonVisible(btnAuctionList, true);
            setButtonVisible(btnUserMgmt, true);
            handleShowUserMgmt(); // Mặc định mở Quản lý người dùng cho Admin
        }
    }

    // Hàm tiện ích bật/tắt nút
    private void setButtonVisible(Button btn, boolean isVisible) {
        if (btn != null) {
            btn.setVisible(isVisible);
            // Quan trọng: setManaged(false) để Layout tự động thu hồi khoảng trống của nút bị ẩn
            btn.setManaged(isVisible);
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
        // Tải màn hình Dashboard của Bidder
        loadContent("Dashboard.fxml");
    }

    @FXML
    private void handleShowBidHistory() {
        // Tải màn hình Lịch sử đấu giá của Bidder
        loadContent("BidHistory.fxml");
    }

    @FXML
    private void handleShowProductMgmt() {
        // Tải màn hình Quản lý sản phẩm của Seller
        loadContent("ProductManagement.fxml");
    }

    @FXML
    private void handleShowUserMgmt() {
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