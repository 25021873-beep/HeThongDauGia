package com.auction.client.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

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
    @FXML private TextField txtDuration;

    private ObservableList<String[]> productData;

    @FXML
    public void initialize() {
        //thiet lap column
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colStartPrice.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colStartTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colEndTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));
        colStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[5]));

        //danh muc spham
        cboCategory.setItems(FXCollections.observableArrayList("Điện tử", "Nghệ thuật", "Xe cộ", "Đồ cổ", "Khác"));

        //nap du lieu fake
        loadMockProducts();
    }

    private void loadMockProducts() {
        productData = FXCollections.observableArrayList(
                new String[]{"Laptop Gaming ASUS ROG", "Điện tử", "12,000,000", "2025-05-10 14:00", "2025-05-10 15:00", "RUNNING"},
                new String[]{"Bức tranh sơn dầu phong cảnh", "Nghệ thuật", "3,000,000", "2025-05-09 10:00", "2025-05-09 22:00", "FINISHED"},
                new String[]{"iPhone 15 Pro Max 256GB", "Điện tử", "20,000,000", "2025-05-11 09:00", "2025-05-11 21:00", "OPEN"},
                new String[]{"Honda SH 150i 2024", "Xe cộ", "35,000,000", "2025-05-12 08:00", "2025-05-12 20:00", "OPEN"}
        );
        tableProducts.setItems(productData);
    }

    @FXML
    private void handleAddProduct() {
        //add spham
        formPane.setExpanded(true);
        clearForm();
    }

    @FXML
    private void handleSaveProduct() {
        String name = txtProductName.getText().trim();
        String category = cboCategory.getValue();
        String description = txtDescription.getText().trim();
        String price = txtStartPrice.getText().trim();
        String duration = txtDuration.getText().trim();

        //ktra dlieu
        if (name.isEmpty() || category == null || price.isEmpty() || duration.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng điền đầy đủ thông tin sản phẩm!");
            return;
        }

        try {
            Double.parseDouble(price);
            Integer.parseInt(duration);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá khởi điểm và thời gian phải là số hợp lệ!");
            return;
        }

        //them spham mock
        String formattedPrice = String.format("%,.0f", Double.parseDouble(price));
        productData.add(new String[]{name, category, formattedPrice, "Chưa bắt đầu", "Chưa xác định", "OPEN"});

        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm sản phẩm \"" + name + "\" thành công!");
        formPane.setExpanded(false);
        clearForm();
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
        txtDuration.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
