package com.auction.client.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

public class UserManagementController {

    @FXML private TableView<String[]> tableUsers;
    @FXML private TableColumn<String[], String> colUsername;
    @FXML private TableColumn<String[], String> colFullName;
    @FXML private TableColumn<String[], String> colEmail;
    @FXML private TableColumn<String[], String> colRole;
    @FXML private TableColumn<String[], String> colUserStatus;
    @FXML private TableColumn<String[], String> colAction;

    private ObservableList<String[]> userData;

    @FXML
    public void initialize() {
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colFullName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colUserStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

        //action column: khoa/mo khoa
        colAction.setCellFactory(col -> new TableCell<String[], String>() {
            private final Button btnToggle = new Button();

            {
                btnToggle.setStyle("-fx-background-radius: 5; -fx-cursor: hand; -fx-font-size: 12px;");
                btnToggle.setPadding(new Insets(4, 10, 4, 10));
                btnToggle.setOnAction(e -> {
                    String[] row = getTableView().getItems().get(getIndex());
                    String targetUsername = row[0];
                    boolean currentStatusIsLocked = "Bị khóa".equals(row[4]);
                    boolean newLockStatus = !currentStatusIsLocked;

                    Thread t = new Thread(() -> {
                        try {
                            com.auction.client.network.ConnectionManager conn = com.auction.client.network.ConnectionManager.getInstance();
                            if (!conn.isConnected()) return;

                            com.google.gson.JsonObject req = new com.google.gson.JsonObject();
                            req.addProperty("command", "TOGGLE_USER_STATUS");
                            req.addProperty("targetUsername", targetUsername);
                            req.addProperty("lockStatus", newLockStatus);

                            com.google.gson.JsonObject res = conn.sendAndWait(req);

                            javafx.application.Platform.runLater(() -> {
                                if (com.auction.client.network.ServerClient.isSuccess(res)) {
                                    row[4] = newLockStatus ? "Bị khóa" : "Hoạt động";
                                    showAlert("Thành công: " + com.auction.client.network.ServerClient.messageOf(res));
                                    getTableView().refresh();
                                } else {
                                    showAlert("Lỗi: " + com.auction.client.network.ServerClient.messageOf(res));
                                }
                            });
                        } catch (java.io.IOException ex) {
                            javafx.application.Platform.runLater(() -> showAlert("Lỗi kết nối khi thay đổi trạng thái"));
                        }
                    });
                    t.setDaemon(true);
                    t.start();
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getIndex() >= getTableView().getItems().size()) {
                    setGraphic(null);
                } else {
                    String[] row = getTableView().getItems().get(getIndex());
                    if ("Hoạt động".equals(row[4])) {
                        btnToggle.setText("🔒 Khóa");
                        btnToggle.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
                    } else {
                        btnToggle.setText("🔓 Mở khóa");
                        btnToggle.setStyle("-fx-background-color: #2E8B57; -fx-text-fill: white; -fx-background-radius: 6; -fx-cursor: hand; -fx-font-size: 12px;");
                    }
                    HBox box = new HBox(btnToggle);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
        });

        loadUsersFromServer();
    }

    private void loadUsersFromServer() {
        userData = FXCollections.observableArrayList();
        tableUsers.setItems(userData);
        
        Thread t = new Thread(() -> {
            try {
                com.auction.client.network.ConnectionManager conn = com.auction.client.network.ConnectionManager.getInstance();
                if (!conn.isConnected()) return;

                com.google.gson.JsonObject req = new com.google.gson.JsonObject();
                req.addProperty("command", "GET_ALL_USERS");

                com.google.gson.JsonObject res = conn.sendAndWait(req);

                javafx.application.Platform.runLater(() -> {
                    if (com.auction.client.network.ServerClient.isSuccess(res)) {
                        userData.clear();
                        if (res.has("users")) {
                            res.getAsJsonArray("users").forEach(elem -> {
                                com.google.gson.JsonObject obj = elem.getAsJsonObject();
                                String username = obj.has("username") ? obj.get("username").getAsString() : "";
                                String fullName = obj.has("fullName") ? obj.get("fullName").getAsString() : "";
                                String email = obj.has("email") ? obj.get("email").getAsString() : "";
                                String role = obj.has("role") ? obj.get("role").getAsString() : "";
                                String status = obj.has("status") ? obj.get("status").getAsString() : "Hoạt động";
                                
                                userData.add(new String[]{username, fullName, email, role, status});
                            });
                        }
                    } else {
                        showAlert("Lỗi: " + com.auction.client.network.ServerClient.messageOf(res));
                    }
                });
            } catch (java.io.IOException e) {
                javafx.application.Platform.runLater(() -> showAlert("Lỗi kết nối khi lấy danh sách user"));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
