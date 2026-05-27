package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.auction.client.network.ServerClient;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class RegisterController {

    @FXML private TextField txtFullName;
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private PasswordField txtConfirmPassword;
    @FXML private ComboBox<String> cboRole;

    @FXML
    public void initialize() {
        cboRole.setItems(FXCollections.observableArrayList("Bidder", "Seller"));
        cboRole.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        String fullName = txtFullName.getText().trim();
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();
        String confirmPassword = txtConfirmPassword.getText();
        String role = cboRole.getValue();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email không hợp lệ!");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        if (password.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải có ít nhất 3 ký tự!");
            return;
        }

        if (role == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn vai trò!");
            return;
        }

        // Gửi request REGISTER lên server
        Thread registerThread = new Thread(() -> {
            try {
                ConnectionManager conn = ConnectionManager.getInstance();
                if (!conn.isConnected()) {
                    conn.connect("127.0.0.1", 8888);
                }

                JsonObject request = new JsonObject();
                request.addProperty("command", "REGISTER");
                request.addProperty("username", username);
                request.addProperty("password", password);
                request.addProperty("email", email);
                request.addProperty("role", role.toUpperCase()); // "BIDDER" hoặc "SELLER"

                JsonObject response = conn.sendAndWait(request);

                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(response)) {
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký tài khoản thành công!\nBạn có thể đăng nhập ngay.");
                        handleGoToLogin(event);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", ServerClient.messageOf(response));
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể kết nối đến server: " + e.getMessage()));
            }
        });
        registerThread.setDaemon(true);
        registerThread.start();
    }

    @FXML
    public void handleGoToLogin(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle("Hệ thống Đấu giá trực tuyến - Đăng nhập");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Không thể tải màn hình đăng nhập!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
