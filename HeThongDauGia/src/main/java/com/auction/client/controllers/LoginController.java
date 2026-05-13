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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;
import com.auction.client.SocketClient;

public class LoginController {

    private SocketClient socketClient = SocketClient.getInstance();

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText();
        String password = txtPassword.getText();

        try {
            socketClient.connect();
            socketClient.login(username, password);
            
            // Lắng nghe phản hồi từ server (nếu cần)
            socketClient.setOnMessageReceived(message -> {
                System.out.println("Server trả về: " + message);
                // Xử lý logic khi server phản hồi (ví dụ: đăng nhập thành công hay thất bại)
            });
            
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Lỗi kết nối", "Không thể kết nối đến server: " + e.getMessage());
            // Có thể return ở đây nếu muốn bắt buộc phải có mạng để đăng nhập
        }

        //xac thuc nguoi dung (tạm thời vẫn dùng fake data của bạn)
        String userRole = authenticateUser(username, password);

        if (userRole != null) {
            //dnhap tcong -> mainlayout
            loadMainLayout(event, userRole);
        } else {
            //dnhap fail -> loi
            showAlert("Đăng nhập thất bại", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
    }

    //fake data
    private String authenticateUser(String username, String password) {
        if ("bidder".equals(username) && "123".equals(password)) return "Bidder";
        if ("seller".equals(username) && "123".equals(password)) return "Seller";
        if ("admin".equals(username) && "123".equals(password)) return "Admin";

        return null;
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

            // Tạo Scene mới với MainLayout và thiết lập lên Stage
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle("Hệ thống Đấu giá trực tuyến - " + role);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen(); // Đưa cửa sổ ra giữa màn hình
            stage.show();

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
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle("Hệ thống Đấu giá trực tuyến - Đăng ký");
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
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