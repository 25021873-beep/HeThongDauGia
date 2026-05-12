package com.auction.client.controllers;

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
        //dsach vai tro
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

        //kiem tra cac field bat buoc
        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin!");
            return;
        }

        //kiem tra email hople
        if (!email.contains("@") || !email.contains(".")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Email không hợp lệ!");
            return;
        }

        //ktra mk khop
        if (!password.equals(confirmPassword)) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu xác nhận không khớp!");
            return;
        }

        //ktra do dai mk
        if (password.length() < 3) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Mật khẩu phải có ít nhất 3 ký tự!");
            return;
        }
//ktra role
        if (role == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn vai trò!");
            return;
        }

        //dki thanh cong
        System.out.println("Đăng ký thành công: " + username + " / " + role);
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng ký tài khoản thành công!\nBạn có thể đăng nhập ngay.");

        //chuyen ve mh dang nhap
        handleGoToLogin(event);
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
