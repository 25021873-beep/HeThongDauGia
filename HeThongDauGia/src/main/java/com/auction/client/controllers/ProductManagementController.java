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
    @FXML private TextField txtStepPrice;
    @FXML private TextField txtStartTime;
    @FXML private TextField txtEndTime;

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

        // Tự động format số tiền với dấu phẩy
        MoneyFieldFormatter.apply(txtStartPrice);
        MoneyFieldFormatter.apply(txtStepPrice);

        // Gọi API lấy danh sách sản phẩm của Seller
        loadSellerProducts();
    }

    private void loadSellerProducts() {
        Thread t = new Thread(() -> {
            try {
                ConnectionManager conn = ConnectionManager.getInstance();
                if (!conn.isConnected()) return;

                JsonObject req = new JsonObject();
                req.addProperty("command", "GET_SELLER_ITEMS");

                JsonObject res = conn.sendAndWait(req);

                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(res)) {
                        productData.clear();
                        if (res.has("items")) {
                            res.getAsJsonArray("items").forEach(elem -> {
                                JsonObject item = elem.getAsJsonObject();
                                String name = item.has("name") ? item.get("name").getAsString() : "";
                                String type = item.has("itemType") ? item.get("itemType").getAsString() : "";
                                String status = item.has("auction_status") ? item.get("auction_status").getAsString() : (item.has("status") ? item.get("status").getAsString() : "");
                                // Dịch mã trạng thái tiếng Anh sang tiếng Việt (phòng trường hợp server trả mã thô)
                                status = translateStatus(status);
                                double price = item.has("startingPrice") ? item.get("startingPrice").getAsDouble() : 0;
                                String formattedPrice = String.format("%,.0f", price);
                                String startT = item.has("start_time") ? item.get("start_time").getAsString() : "N/A";
                                String endT = item.has("end_time") ? item.get("end_time").getAsString() : "N/A";
                                
                                productData.add(new String[]{name, type, formattedPrice, startT, endT, status});
                            });
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", ServerClient.messageOf(res));
                    }
                });
            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể lấy danh sách sản phẩm"));
            }
        });
        t.setDaemon(true);
        t.start();
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
        String priceText = MoneyFieldFormatter.getRawValue(txtStartPrice);
        String stepPriceText = MoneyFieldFormatter.getRawValue(txtStepPrice);

        String startTimeText = txtStartTime.getText().trim();
        String endTimeText = txtEndTime.getText().trim();

        if (name.isEmpty() || category == null || priceText.isEmpty() || startTimeText.isEmpty() || endTimeText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin sản phẩm và thời gian!");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá khởi điểm phải là số hợp lệ!");
            return;
        }

        double parsedStepPrice = 0;
        if (!stepPriceText.isEmpty()) {
            try {
                parsedStepPrice = Double.parseDouble(stepPriceText);
                if (parsedStepPrice < 0) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Bước giá tối thiểu không được âm!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Bước giá tối thiểu phải là số hợp lệ!");
                return;
            }
        }
        final double stepPrice = parsedStepPrice;

        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        java.time.LocalDateTime startTime;
        java.time.LocalDateTime endTime;
        try {
            startTime = java.time.LocalDateTime.parse(startTimeText, formatter);
            endTime = java.time.LocalDateTime.parse(endTimeText, formatter);
            if (!startTime.isBefore(endTime)) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thời gian bắt đầu phải trước thời gian kết thúc!");
                return;
            }
        } catch (java.time.format.DateTimeParseException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Định dạng thời gian không hợp lệ. Vui lòng dùng YYYY-MM-DD HH:mm");
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
                req.addProperty("starting_price", price);
                req.addProperty("start_time", startTimeText);
                req.addProperty("end_time", endTimeText);
                req.addProperty("step_price", stepPrice);

                // Thêm các thuộc tính giả định cho subclass để tránh lỗi Gson khi deserialize
                if ("ELECTRONICS".equals(itemType)) req.addProperty("warrantyMonths", 12);
                if ("ART".equals(itemType)) req.addProperty("author", "Unknown");
                if ("VEHICLE".equals(itemType)) req.addProperty("engineType", "Standard");

                JsonObject res = conn.sendAndWait(req);

                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(res)) {
                        String formattedPrice = String.format("%,.0f", price);
                        productData.add(new String[]{name, category, formattedPrice, startTimeText, endTimeText, "Đang xử lý"});
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đăng bán sản phẩm \"" + name + "\" thành công!");
                        formPane.setExpanded(false);
                        clearForm();
                        loadSellerProducts(); // Refresh danh sách sau khi thêm
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
        if (txtStartTime != null) txtStartTime.clear();
        if (txtEndTime != null) txtEndTime.clear();
        if (txtStepPrice != null) txtStepPrice.clear();
    }

    private String mapCategoryToItemType(String category) {
        switch (category) {
            case "Nghệ thuật": return "ART";
            case "Xe cộ": return "VEHICLE";
            default: return "ELECTRONICS";
        }
    }

    private String translateStatus(String status) {
        if (status == null) return "";
        switch (status) {
            case "OPEN":     return "Sắp bắt đầu";
            case "RUNNING":  return "Đang diễn ra";
            case "FINISHED": return "Đã kết thúc";
            case "CANCELED": return "Đã hủy";
            case "PAID":     return "Đã thanh toán";
            default:         return status; // Giữ nguyên nếu đã là tiếng Việt
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        if (type == Alert.AlertType.ERROR) {
            ToastManager.showError(message);
        } else {
            ToastManager.showInfo(message);
        }
    }
}
