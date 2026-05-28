package com.auction.client.controllers;

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
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectedLoginController {
    @FXML private TextField txtUsername;
    @FXML private PasswordField txtPassword;
    @FXML private ComboBox<String> cboRole;

    @FXML
    public void initialize() {
        cboRole.setItems(FXCollections.observableArrayList("BIDDER", "SELLER"));
        cboRole.getSelectionModel().selectFirst();
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Dang nhap that bai", "Vui long nhap username va password.");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("command", "LOGIN");
        request.addProperty("username", username);
        request.addProperty("password", password);

        sendAsync(request, response -> {
            if (ServerClient.isSuccess(response) && response.has("role")) {
                loadMainLayout(event, response.get("role").getAsString());
            } else {
                showAlert(Alert.AlertType.ERROR, "Dang nhap that bai", ServerClient.messageOf(response));
            }
        });
    }

    @FXML
    public void handleRegister() {
        String username = txtUsername.getText().trim();
        String password = txtPassword.getText().trim();
        String role = cboRole.getValue();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Dang ky that bai", "Vui long nhap username va password.");
            return;
        }

        JsonObject request = new JsonObject();
        request.addProperty("command", "REGISTER");
        request.addProperty("username", username);
        request.addProperty("password", password);
        request.addProperty("role", role);

        sendAsync(request, response -> {
            if (ServerClient.isSuccess(response)) {
                showAlert(Alert.AlertType.INFORMATION, "Dang ky thanh cong", ServerClient.messageOf(response));
            } else {
                showAlert(Alert.AlertType.ERROR, "Dang ky that bai", ServerClient.messageOf(response));
            }
        });
    }

    private void sendAsync(JsonObject request, ResponseHandler handler) {
        Thread thread = new Thread(() -> {
            try {
                JsonObject response = ServerClient.sendRequest(request);
                Platform.runLater(() -> handler.handle(response));
            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Loi ket noi",
                        "Khong the ket noi den server " + ServerClient.endpoint() + ". Hay chay ServerMain truoc.\n"
                                + e.getMessage()));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void loadMainLayout(ActionEvent event, String role) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/MainLayout.fxml"));
            Parent root = loader.load();

            MainController mainController = loader.getController();
            mainController.configureSidebar(role);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            stage.setTitle("He thong Dau gia - " + role);
            stage.setScene(scene);
            stage.setResizable(true);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Loi giao dien", "Khong the tai MainLayout: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private interface ResponseHandler {
        void handle(JsonObject response);
    }
}
