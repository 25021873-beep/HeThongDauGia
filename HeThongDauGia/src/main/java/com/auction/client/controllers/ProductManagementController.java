package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.auction.client.network.ServerClient;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;

public class ProductManagementController {

    @FXML private TableView<String[]> tableProducts;
    @FXML private TableColumn<String[], String> colName;
    @FXML private TableColumn<String[], String> colCategory;
    @FXML private TableColumn<String[], String> colStartPrice;
    @FXML private TableColumn<String[], String> colStartTime;
    @FXML private TableColumn<String[], String> colEndTime;
    @FXML private TableColumn<String[], String> colStatus;

    @FXML private TitledPane formPane;
    @FXML private TextField txtProductName;
    @FXML private ComboBox<String> cboCategory;
    @FXML private TextArea txtDescription;
    @FXML private TextField txtStartPrice;
    @FXML private TextField txtDuration; // Không thực sự dùng vì Item không lưu duration

    private ObservableList<String[]> productData;

    @FXML
    public void initialize() {
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colStartPrice.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colStartTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colEndTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[5]));

        cboCategory.setItems(FXCollections.observableArrayList("Điện tử", "Nghệ thuật", "Xe cộ"));
        productData = FXCollections.observableArrayList();
        tableProducts.setItems(productData);

        // Lưu ý: Chưa có API để lấy danh sách sản phẩm của Seller, nên bảng sẽ trống lúc đầu
    }

    @FXML
    private void handleAddProduct() {
        formPane.setExpanded(true);
        clearForm();
    }

    @FXML
    private void handleSaveProduct() {
        String name = txtProductName.getText().trim();
        String category = cboCategory.getValue();
        String description = txtDescription.getText().trim();
        String priceText = txtStartPrice.getText().trim();

        if (name.isEmpty() || category == null || priceText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin sản phẩm!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá khởi điểm phải là số hợp lệ!");
            return;
        }

        String itemType = mapCategoryToItemType(category);

        Thread t = new Thread(() -> {
            try {
                ConnectionManager conn = ConnectionManager.getInstance();
                JsonObject req = new JsonObject();
                req.addProperty("command", "POST_ITEM");
                req.addProperty("itemType", itemType);
                req.addProperty("name", name);
                req.addProperty("description", description);
                req.addProperty("startingPrice", price);

                // Thêm các thuộc tính giả định cho subclass để tránh lỗi Gson khi deserialize
                if ("ELECTRONICS".equals(itemType)) req.addProperty("warrantyMonths", 12);
                if ("ART".equals(itemType)) req.addProperty("author", "Unknown");
                if ("VEHICLE".equals(itemType)) req.addProperty("engineType", "Standard");

                JsonObject res = conn.sendAndWait(req);

                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(res)) {
                        String formattedPrice = String.format("%,.0f", price);
                        productData.add(new String[]{name, category, formattedPrice, "N/A", "N/A", "AVAILABLE"});
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng bán sản phẩm \"" + name + "\" thành công!");
                        formPane.setExpanded(false);
                        clearForm();
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi thêm sản phẩm", ServerClient.messageOf(res));
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", e.getMessage()));
            }
        });
        t.setDaemon(true);
        t.start();
    }

    @FXML
    private void handleCancelEdit() {
        formPane.setExpanded(false);
        clearForm();
    }

    private void clearForm() {
        txtProductName.clear();
        cboCategory.getSelectionModel().clearSelection();
        txtDescription.clear();
        txtStartPrice.clear();
        if (txtDuration != null) txtDuration.clear();
    }

    private String mapCategoryToItemType(String category) {
        switch (category) {
            case "Nghệ thuật": return "ART";
            case "Xe cộ": return "VEHICLE";
            default: return "ELECTRONICS";
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
