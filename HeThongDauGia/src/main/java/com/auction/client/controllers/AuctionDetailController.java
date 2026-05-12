package com.auction.client.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class AuctionDetailController {

    @FXML private Label lblProductName;
    @FXML private Label lblCategory;
    @FXML private Label lblDescription;
    @FXML private Label lblSeller;
    @FXML private Label lblStatus;
    @FXML private Label lblCountdown;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblLeader;

    @FXML private TextField txtBidAmount;
    @FXML private TextField txtMaxBid;
    @FXML private TextField txtIncrement;

    @FXML private Button btnPlaceBid;
    @FXML private Button btnAutoBid;
    @FXML private Button btnBack;

    @FXML private LineChart<String, Number> priceChart;
    @FXML private CategoryAxis xAxis;
    @FXML private NumberAxis yAxis;

    @FXML private TableView<String[]> tableBids;
    @FXML private TableColumn<String[], String> colBidder;
    @FXML private TableColumn<String[], String> colAmount;
    @FXML private TableColumn<String[], String> colTime;

    private double currentPrice = 15000000;
    private String currentLeader = "bidder02";
    private int remainingSeconds = 930; // 15p30s
    private Timeline countdownTimeline;
    private XYChart.Series<String, Number> priceSeries;
    private ObservableList<String[]> bidData;
    private boolean autoBidEnabled = false;


    private String productName = "Laptop Gaming ASUS ROG Strix";
    private String productDesc = "Laptop gaming cao cấp với RTX 4070, RAM 16GB, SSD 1TB. Tình trạng mới 99%.";
    private String productCategory = "Điện tử";
    private String productSeller = "seller01";

    @FXML
    public void initialize() {
        //ttin spham
        lblProductName.setText(productName);
        lblCategory.setText("Danh mục: " + productCategory);
        lblDescription.setText(productDesc);
        lblSeller.setText("Người bán: " + productSeller);
        updatePriceDisplay();

        //lsu bid
        colBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        //dlieu fake
        bidData = FXCollections.observableArrayList(
                new String[]{"bidder02", "15,000,000 VNĐ", "14:28:30"},
                new String[]{"bidder01", "14,500,000 VNĐ", "14:25:15"},
                new String[]{"bidder03", "14,000,000 VNĐ", "14:20:45"},
                new String[]{"bidder02", "13,500,000 VNĐ", "14:15:20"},
                new String[]{"bidder01", "13,000,000 VNĐ", "14:10:05"},
                new String[]{"bidder03", "12,500,000 VNĐ", "14:05:30"}
        );
        tableBids.setItems(bidData);

        //bieudogia
        setupPriceChart();


        startCountdown();
    }


    public void setProductInfo(String name, String description, double price, String category, String seller) {
        this.productName = name;
        this.productDesc = description;
        this.currentPrice = price;
        this.productCategory = category;
        this.productSeller = seller;

        lblProductName.setText(name);
        lblCategory.setText("Danh mục: " + category);
        lblDescription.setText(description);
        lblSeller.setText("Người bán: " + seller);
        updatePriceDisplay();
    }

    private void setupPriceChart() {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Giá đấu cao nhất");

        //dlieu fake
        priceSeries.getData().add(new XYChart.Data<>("14:05", 12500000));
        priceSeries.getData().add(new XYChart.Data<>("14:10", 13000000));
        priceSeries.getData().add(new XYChart.Data<>("14:15", 13500000));
        priceSeries.getData().add(new XYChart.Data<>("14:20", 14000000));
        priceSeries.getData().add(new XYChart.Data<>("14:25", 14500000));
        priceSeries.getData().add(new XYChart.Data<>("14:28", 15000000));

        priceChart.getData().add(priceSeries);
        priceChart.setCreateSymbols(true);
        priceChart.setAnimated(false);
    }

    private void startCountdown() {
        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (remainingSeconds > 0) {
                remainingSeconds--;
                int minutes = remainingSeconds / 60;
                int seconds = remainingSeconds % 60;
                lblCountdown.setText(String.format("⏱ %02d:%02d", minutes, seconds));
            } else {
                countdownTimeline.stop();
                lblCountdown.setText("⏱ HẾT GIỜ");
                lblStatus.setText("🔴 FINISHED");
                lblStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #888888;");
                btnPlaceBid.setDisable(true);
                btnAutoBid.setDisable(true);
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    @FXML
    private void handlePlaceBid() {
        String bidText = txtBidAmount.getText().trim();
        if (bidText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập giá đấu!");
            return;
        }

        double bidAmount;
        try {
            bidAmount = Double.parseDouble(bidText);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá đấu phải là một số hợp lệ!");
            return;
        }


        if (remainingSeconds <= 0) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Phiên đấu giá đã kết thúc!");
            return;
        }


        if (bidAmount <= currentPrice) {
            showAlert(Alert.AlertType.ERROR, "Lỗi đặt giá",
                    String.format("Giá đấu phải cao hơn giá hiện tại (%,.0f VNĐ)!", currentPrice));
            return;
        }


        currentPrice = bidAmount;
        currentLeader = "Bạn";
        updatePriceDisplay();

        //them vao bang
        String timeStr = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        bidData.add(0, new String[]{"Bạn", String.format("%,.0f VNĐ", bidAmount), timeStr});


        priceSeries.getData().add(new XYChart.Data<>(timeStr, bidAmount));

        txtBidAmount.clear();
        showAlert(Alert.AlertType.INFORMATION, "Thành công",
                String.format("Đặt giá thành công: %,.0f VNĐ\nBạn đang dẫn đầu!", bidAmount));
    }

    @FXML
    private void handleAutoBid() {
        if (autoBidEnabled) {
            //tat auto bid
            autoBidEnabled = false;
            btnAutoBid.setText("⚡ Bật Auto-Bid");
            btnAutoBid.setStyle("-fx-background-color: #FCBF49; -fx-text-fill: #1A1A1A; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            showAlert(Alert.AlertType.INFORMATION, "Auto-Bid", "Đã tắt Auto-Bid.");
            return;
        }

        String maxBidText = txtMaxBid.getText().trim();
        String incrementText = txtIncrement.getText().trim();

        if (maxBidText.isEmpty() || incrementText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập giá tối đa và bước giá!");
            return;
        }

        try {
            double maxBid = Double.parseDouble(maxBidText);
            double increment = Double.parseDouble(incrementText);

            if (maxBid <= currentPrice) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá tối đa phải cao hơn giá hiện tại!");
                return;
            }

            if (increment <= 0) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Bước giá phải lớn hơn 0!");
                return;
            }

            autoBidEnabled = true;
            btnAutoBid.setText("🛑 Tắt Auto-Bid");
            btnAutoBid.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            showAlert(Alert.AlertType.INFORMATION, "Auto-Bid",
                    String.format("Đã bật Auto-Bid!\nGiá tối đa: %,.0f VNĐ\nBước giá: %,.0f VNĐ", maxBid, increment));

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá tối đa và bước giá phải là số hợp lệ!");
        }
    }

    @FXML
    private void handleBack() {
        // Dừng countdown trước khi quay lại
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Node dashboard = loader.load();

            // Lấy contentArea từ MainLayout (StackPane cha)
            StackPane contentArea = (StackPane) btnBack.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(dashboard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void updatePriceDisplay() {
        lblCurrentPrice.setText(String.format("%,.0f VNĐ", currentPrice));
        lblLeader.setText("Người dẫn đầu: " + currentLeader);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
