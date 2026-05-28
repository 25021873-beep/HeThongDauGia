package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.auction.client.network.ServerClient;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class DepositController {

    @FXML private TextField txtAmount;
    @FXML private Button btnConfirm;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleDeposit(ActionEvent event) {
        String amountText = txtAmount.getText().trim();
        if (amountText.isEmpty()) {
            showAlert("Lỗi", "Vui lòng nhập số tiền cần nạp.");
            return;
        }

        double amount = 0;
        try {
            amount = Double.parseDouble(amountText);
            if (amount <= 0) {
                showAlert("Lỗi", "Số tiền phải lớn hơn 0.");
                return;
            }
        } catch (NumberFormatException e) {
            showAlert("Lỗi", "Số tiền không hợp lệ.");
            return;
        }

        final double depositAmount = amount;

        Thread depositThread = new Thread(() -> {
            try {
                ConnectionManager conn = ConnectionManager.getInstance();
                JsonObject request = new JsonObject();
                request.addProperty("command", "TOP_UP");
                request.addProperty("amount", depositAmount);

                JsonObject response = conn.sendAndWait(request);

                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(response)) {
                        // Cập nhật lại số dư trong ConnectionManager
                        double newBalance = response.has("newBalance") ? response.get("newBalance").getAsDouble() : (conn.getBalance() + depositAmount);
                        conn.setBalance(newBalance);
                        
                        // Cập nhật giao diện MainController
                        if (mainController != null) {
                            mainController.updateBalanceDisplay();
                        }
                        
                        // Đóng cửa sổ nạp tiền
                        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                        stage.close();
                        
                        showAlert("Thành công", "Nạp tiền thành công! Số dư mới: " + String.format("%,.0f VND", newBalance));
                    } else {
                        showAlert("Nạp tiền thất bại", ServerClient.messageOf(response));
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert("Lỗi kết nối", "Không thể kết nối đến server: " + e.getMessage()));
            }
        });
        depositThread.setDaemon(true);
        depositThread.start();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        if (title.contains("Lỗi") || title.contains("thất bại")) {
            alert.setAlertType(Alert.AlertType.ERROR);
        }
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
