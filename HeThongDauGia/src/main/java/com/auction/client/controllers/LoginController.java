package com.auction.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        // 1. Xác thực người dùng và lấy Role (Tạm thời dùng Mock Data)
        String userRole = authenticateUser(username, password);

        if (userRole != null) {
            // 2. Nếu đăng nhập thành công, chuyển sang MainLayout
            loadMainLayout(event, userRole);
        } else {
            // 3. Nếu sai tài khoản/mật khẩu, hiện thông báo lỗi
            showAlert("Đăng nhập thất bại", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
    }

    /**
     * Hàm giả lập (Mock) gọi Server để xác thực.
     * Sau này bạn sẽ thay bằng logic gọi qua Socket hoặc REST API.
     */
    private String authenticateUser(String username, String password) {
        // Tài khoản mặc định: mật khẩu là "123"
        if ("bidder".equals(username) && "123".equals(password)) return "Bidder";
        if ("seller".equals(username) && "123".equals(password)) return "Seller";
        if ("admin".equals(username) && "123".equals(password)) return "Admin";

        return null; // Trả về null nếu sai
    }

    /**
     * Nạp MainLayout và chuyển cảnh (Scene Switch)
     */
    private void loadMainLayout(ActionEvent event, String role) {
        try {
            // Tải file bộ khung chung MainLayout.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
            Parent root = loader.load();

            // Lấy MainController để cấu hình Sidebar dựa theo Role
            MainController mainController = loader.getController();
            mainController.configureSidebar(role);

            // Lấy Stage (cửa sổ) hiện tại từ nút bấm
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Tạo Scene mới với MainLayout và thiết lập lên Stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.centerOnScreen(); // Đưa cửa sổ ra giữa màn hình
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể tải giao diện Dashboard: " + e.getMessage());
        }
    }

    /**
     * Hàm tiện ích để hiển thị thông báo lỗi (Alert Dialog)
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}