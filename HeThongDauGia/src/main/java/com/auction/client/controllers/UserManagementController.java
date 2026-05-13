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
                    if ("Hoạt động".equals(row[4])) {
                        row[4] = "Bị khóa";
                        showAlert("Đã khóa tài khoản: " + row[0]);
                    } else {
                        row[4] = "Hoạt động";
                        showAlert("Đã mở khóa tài khoản: " + row[0]);
                    }
                    getTableView().refresh();
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

        loadMockUsers();
    }

    private void loadMockUsers() {
        userData = FXCollections.observableArrayList(
                new String[]{"bidder", "Phùng Thanh Độ", "dochet1989@email.com", "Bidder", "Hoạt động"},
                new String[]{"bidder02", "Ngô Đức Minh", "minhngu67@email.com", "Bidder", "Hoạt động"},
                new String[]{"seller", "Đỗ Tuấn Dương", "cuongduong69@email.com", "Seller", "Hoạt động"},
                new String[]{"seller02", "Đỗ Trọng Nghĩa", "nghiatinh@email.com", "Seller", "Bị khóa"},
                new String[]{"bidder03", "Hoàng Thị Hà Linh", "hhloz@email.com", "Bidder", "Hoạt động"},
                new String[]{"seller03", "Bùi Phương Linh", "120yenlang1@email.com", "Seller", "Hoạt động"},
                new String[]{"seller04", "Phạm Minh Ngọc", "vinhomesoceanpark@email.com", "Seller", "Hoạt động"},
                new String[]{"bidder03", "Trần Viết Anh", "hadong@email.com", "Seller", "Hoạt động"},
                new String[]{"bidder05", "Dương Quỳnh Nga", "ngatuvong@email.com", "Seller", "Hoạt động"}
        );
        tableUsers.setItems(userData);
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
