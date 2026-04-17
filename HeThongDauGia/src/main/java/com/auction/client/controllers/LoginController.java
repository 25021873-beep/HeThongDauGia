package com.auction.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    // Khai báo các biến khớp đúng y hệt với fx:id bên file FXML
    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    // Hàm này sẽ chạy khi ông bấm cái nút có onAction="#handleLogin"
    @FXML
    public void handleLogin(ActionEvent event) {
        // Lấy chữ người dùng nhập
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        // In tạm ra console để test xem có ăn dữ liệu chưa
        System.out.println("Tài khoản: " + user);
        System.out.println("Mật khẩu: " + pass);

        // Sau này có Backend thì mình sẽ gọi API kiểm tra đăng nhập ở đoạn này
        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Lỗi: Không được để trống!");
        } else {
            System.out.println("Bắt đầu gửi data xuống Server...");
        }
    }
}