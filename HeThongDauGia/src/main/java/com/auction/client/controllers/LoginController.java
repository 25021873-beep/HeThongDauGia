package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.auction.client.network.ServerClient;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML private StackPane rootPane;
    @FXML private ImageView bgTexture;
    @FXML private ImageView bgPattern;

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void initialize() {
        // Bind background images to root pane size for fullscreen stretching
        if (rootPane != null && bgTexture != null && bgPattern != null) {
            bgTexture.fitWidthProperty().bind(rootPane.widthProperty());
            bgTexture.fitHeightProperty().bind(rootPane.heightProperty());
            bgPattern.fitWidthProperty().bind(rootPane.widthProperty());
            bgPattern.fitHeightProperty().bind(rootPane.heightProperty());
        }
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập tên đăng nhập và mật khẩu!");
            return;
        }

        // Gửi request LOGIN qua socket trên background thread
        Thread loginThread = new Thread(() -> {
            try {
                // 1. Kết nối server (nếu chưa kết nối)
                ConnectionManager conn = ConnectionManager.getInstance();
                if (!conn.isConnected()) {
                    conn.connectDefault();
                }

                // 2. Gửi JSON LOGIN và đợi response
                JsonObject request = new JsonObject();
                request.addProperty("command", "LOGIN");
                request.addProperty("username", username);
                request.addProperty("password", password);

                JsonObject response = conn.sendAndWait(request);

                // 3. Xử lý response trên JavaFX thread
                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(response)) {
                        // Lưu thông tin user
                        int userId = response.has("userId") ? response.get("userId").getAsInt() : 0;
                        String role = response.has("role") ? response.get("role").getAsString() : "BIDDER";
                        double balance = response.has("balance") && !response.get("balance").isJsonNull() ? response.get("balance").getAsDouble() : 0.0;
                        conn.setUserInfo(userId, username, role, balance);

                        // Chuyển sang MainLayout
                        loadMainLayout(event, role);
                    } else {
                        showAlert("Đăng nhập thất bại", ServerClient.messageOf(response));
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> {
                    showAlert("Lỗi kết nối",
                            "Không thể kết nối đến server " + ConnectionManager.getDefaultEndpoint() + ".\n"
                                    + "Hãy chạy ServerMain trước.\n" + e.getMessage());
                });
            }
        });
        loginThread.setDaemon(true);
        loginThread.start();
    }

    private void loadMainLayout(ActionEvent event, String role) {
        try {
            //load mainlayout
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
            Parent root = loader.load();

            //load maincontroller de lay sidebar theo role
            MainController mainController = loader.getController();
            mainController.configureSidebar(role);

            //lay stage tu button
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Swap root của Scene hiện tại để giữ nguyên kích thước cửa sổ
            stage.getScene().setRoot(root);
            stage.setTitle("Hệ thống Đấu giá trực tuyến - " + role);

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể tải giao diện Dashboard: " + e.getMessage());
        }
    }

//mhinh dki
    @FXML
    public void handleGoToRegister(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/Register.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            // Swap root để giữ nguyên kích thước cửa sổ
            stage.getScene().setRoot(root);
            stage.setTitle("Hệ thống Đấu giá trực tuyến - Đăng ký");
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi hệ thống", "Không thể tải màn hình đăng ký: " + e.getMessage());
        }
    }

//hien thi tbao loi
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
