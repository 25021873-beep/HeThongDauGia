package com.auction.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class LoginController {

    @FXML
    private TextField txtUsername;

    @FXML
    private PasswordField txtPassword;

    @FXML
    public void handleLogin(ActionEvent event) {
        String user = txtUsername.getText();
        String pass = txtPassword.getText();

        System.out.println("Tài khoản: " + user);
        System.out.println("Mật khẩu: " + pass);

        if (user.isEmpty() || pass.isEmpty()) {
            System.out.println("Lỗi: Không được để trống!");
        } else {
            System.out.println("Bắt đầu gửi data xuống Server...");
        }
    }
}