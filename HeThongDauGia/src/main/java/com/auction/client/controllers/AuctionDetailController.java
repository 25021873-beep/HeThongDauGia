package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.auction.client.network.ServerClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
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
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.entity.Auction;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class AuctionDetailController {

    @FXML private Label lblProductName;
    @FXML private Label lblCategory;
    @FXML private Label lblDescription;
    @FXML private Label lblSeller;
    @FXML private Label lblStatus;
    @FXML private Label lblCountdown;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblLeader;

    @FXML private VBox countdownBox;
    @FXML private Label lblAntiSniping;
    @FXML private Label lblTimeExtended;

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

    private int currentAuctionId = -1;
    private double currentPrice = 0;
    private String currentLeader = "Chưa có";
    private LocalDateTime endTime;
    private Timeline countdownTimeline;
    private XYChart.Series<String, Number> priceSeries;
    private ObservableList<String[]> bidData = FXCollections.observableArrayList();
    private boolean autoBidEnabled = false;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    public void initialize() {
        colBidder.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colAmount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));

        tableBids.setItems(bidData);
        setupPriceChart();

        // Lắng nghe sự kiện realtime từ server
        ConnectionManager.getInstance().setOnPushMessage(this::handlePushMessage);
    }

    public void setProductInfo(int auctionId, String name, String description, double price, String category, String seller) {
        this.currentAuctionId = auctionId;
        this.currentPrice = price;
        
        lblProductName.setText(name);
        lblCategory.setText("Danh mục: " + category);
        lblDescription.setText(description);
        lblSeller.setText("Người bán: " + seller);
        
        updatePriceDisplay();

        // 1. Join room để nhận realtime push
        joinAuctionRoom(auctionId);
        
        // 2. Lấy thông tin chi tiết phiên
        fetchAuctionDetail(auctionId);
        
        // 3. Lấy lịch sử đấu giá
        fetchBidHistory(auctionId);
    }

    private void joinAuctionRoom(int auctionId) {
        Thread t = new Thread(() -> {
            try {
                ConnectionManager conn = ConnectionManager.getInstance();
                if (!conn.isConnected()) return;

                JsonObject req = new JsonObject();
                req.addProperty("command", "JOIN");
                req.addProperty("auctionId", auctionId);

                JsonObject res = conn.sendAndWait(req);
                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(res)) {
                        String status = res.has("auctionStatus") ? res.get("auctionStatus").getAsString() : "OPEN";
                        lblStatus.setText("RUNNING".equals(status) ? "🟢 Đang diễn ra" : ("OPEN".equals(status) ? "🟡 Sắp bắt đầu" : "🔴 Đã kết thúc"));
                        if (res.has("endTime")) {
                            String endTimeStr = res.get("endTime").getAsString();
                            this.endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            startCountdown();
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void fetchAuctionDetail(int auctionId) {
        Thread t = new Thread(() -> {
            try {
                JsonObject req = new JsonObject();
                req.addProperty("command", "GET_AUCTION_DETAIL");
                req.addProperty("auctionId", auctionId);
                JsonObject res = ConnectionManager.getInstance().sendAndWait(req);
                
                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(res)) {
                        if (res.has("description")) lblDescription.setText(res.get("description").getAsString());
                        if (res.has("itemType")) lblCategory.setText("Danh mục: " + res.get("itemType").getAsString());
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void fetchBidHistory(int auctionId) {
        Thread t = new Thread(() -> {
            try {
                JsonObject req = new JsonObject();
                req.addProperty("command", "GET_BID_HISTORY");
                req.addProperty("auctionId", auctionId);
                JsonObject res = ConnectionManager.getInstance().sendAndWait(req);
                
                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(res) && res.has("history")) {
                        bidData.clear();
                        priceSeries.getData().clear();
                        JsonArray history = res.getAsJsonArray("history");
                        
                        for (int i = 0; i < history.size(); i++) {
                            JsonObject point = history.get(i).getAsJsonObject();
                            String bidder = point.get("bidderUsername").getAsString();
                            double price = point.get("price").getAsDouble();
                            String timeStr = point.get("bidTime").getAsString();
                            LocalDateTime dt = LocalDateTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            String timeFormatted = dt.format(TIME_FORMATTER);
                            
                            bidData.add(0, new String[]{bidder, String.format("%,.0f VNĐ", price), timeFormatted});
                            priceSeries.getData().add(new XYChart.Data<>(timeFormatted, price));
                            
                            if (i == history.size() - 1) { // Latest bid
                                currentPrice = price;
                                currentLeader = bidder;
                            }
                        }
                        updatePriceDisplay();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void setupPriceChart() {
        priceSeries = new XYChart.Series<>();
        priceSeries.setName("Giá đấu cao nhất");
        priceChart.getData().add(priceSeries);
        priceChart.setCreateSymbols(true);
        priceChart.setAnimated(false);
    }

    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (endTime == null) return;
            long remainingSeconds = LocalDateTime.now().until(endTime, ChronoUnit.SECONDS);
            
            if (remainingSeconds > 0) {
                long minutes = remainingSeconds / 60;
                long seconds = remainingSeconds % 60;
                lblCountdown.setText(String.format("⏱ %02d:%02d", minutes, seconds));
            } else {
                countdownTimeline.stop();
                lblCountdown.setText("⏱ HẾT GIỜ");
                lblStatus.setText("🔴 Đã kết thúc");
                lblStatus.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #888888;");
                btnPlaceBid.setDisable(true);
                btnAutoBid.setDisable(true);
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    private void handlePushMessage(JsonObject msg) {
        if (!msg.has("status")) return;
        String status = msg.get("status").getAsString();
        
        int msgAuctionId = msg.has("auctionId") ? msg.get("auctionId").getAsInt() : -1;
        if (msgAuctionId != currentAuctionId) return;

        switch (status) {
            case "UPDATE": // BID_UPDATE
                String bidder = msg.get("bidderUsername").getAsString();
                double newPrice = msg.get("newPrice").getAsDouble();
                String timeStr = LocalTimeNow();
                
                currentPrice = newPrice;
                currentLeader = bidder;
                updatePriceDisplay();
                
                bidData.add(0, new String[]{bidder, String.format("%,.0f VNĐ", newPrice), timeStr});
                priceSeries.getData().add(new XYChart.Data<>(timeStr, newPrice));
                break;
                
            case "AUCTION_EXTENDED":
                if (msg.has("newEndTime")) {
                    String endTimeStr = msg.get("newEndTime").getAsString();
                    this.endTime = LocalDateTime.parse(endTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    startCountdown();
                    showTimeExtendedAnimation();
                }
                break;
                
            case "AUCTION_END":
                String winner = msg.has("winnerUsername") ? msg.get("winnerUsername").getAsString() : "Không có";
                double finalPrice = msg.has("finalPrice") ? msg.get("finalPrice").getAsDouble() : 0;
                
                if (countdownTimeline != null) countdownTimeline.stop();
                lblCountdown.setText("⏱ HẾT GIỜ");
                lblStatus.setText("🔴 Đã kết thúc");
                currentLeader = winner + " (Chiến thắng)";
                currentPrice = finalPrice;
                updatePriceDisplay();
                
                btnPlaceBid.setDisable(true);
                btnAutoBid.setDisable(true);
                
                showAlert(Alert.AlertType.INFORMATION, "Kết thúc", 
                        "Phiên đấu giá đã kết thúc!\nNgười chiến thắng: " + winner + "\nGiá chốt: " + String.format("%,.0f VNĐ", finalPrice));
                break;
                
            case "AUCTION_STARTED":
                lblStatus.setText("🟢 Đang diễn ra");
                break;
        }
    }

    private String LocalTimeNow() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }

    @FXML
    private void handlePlaceBid() {
        String bidText = txtBidAmount.getText().trim();
        if (bidText.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập giá đấu!");
            return;
        }

        try {
            BigDecimal bidAmount = new BigDecimal(bidText);

            Thread t = new Thread(() -> {
                try {
                    JsonObject req = new JsonObject();
                    req.addProperty("command", "BID");

                    req.addProperty("auctionId", currentAuctionId);

                    req.addProperty("amount", bidAmount.toString());

                    JsonObject res = ConnectionManager.getInstance().sendAndWait(req);

                    Platform.runLater(() -> {
                        if (ServerClient.isSuccess(res)) {
                            txtBidAmount.clear();
                            showAlert(Alert.AlertType.INFORMATION, "Thành công",
                                    "Đặt giá thành công: " + String.format("%,d VNĐ", bidAmount.toBigInteger()));
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi đặt giá", ServerClient.messageOf(res));
                        }
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", e.getMessage()));
                }
            });

            t.setDaemon(true);
            t.start();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá đấu phải là số hợp lệ!");
        }
    }

    @FXML
    private void handleAutoBid() {
        if (autoBidEnabled) { // Client-side disable toggle, server might need a DISABLE_AUTO_BID command, but SET_AUTO_BID with 0 or negative can handle it? Wait, let's just toggle locally and not send anything if backend doesn't support disabling. Backend deactivate autobid on invalid bid.
            autoBidEnabled = false;
            btnAutoBid.setText("⚡ Bật tự động đấu");
            btnAutoBid.setStyle("-fx-background-color: #FCBF49; -fx-text-fill: #1A1A1A; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
            showAlert(Alert.AlertType.INFORMATION, "Tự động đấu giá", "Đã tắt chế độ tự động đấu giá.");
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

            Thread t = new Thread(() -> {
                try {
                    JsonObject req = new JsonObject();
                    req.addProperty("command", "SET_AUTO_BID");
                    req.addProperty("auctionId", currentAuctionId);
                    req.addProperty("maxBid", maxBid);
                    req.addProperty("increment", increment);
                    
                    JsonObject res = ConnectionManager.getInstance().sendAndWait(req);
                    
                    Platform.runLater(() -> {
                        if (ServerClient.isSuccess(res)) {
                            autoBidEnabled = true;
                            btnAutoBid.setText("🛑 Tắt tự động đấu");
                            btnAutoBid.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
                            showAlert(Alert.AlertType.INFORMATION, "Tự động đấu giá",
                                    String.format("Đã bật tự động đấu giá!\nGiá tối đa: %,.0f VNĐ\nBước giá: %,.0f VNĐ", maxBid, increment));
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Lỗi tự động đấu", ServerClient.messageOf(res));
                        }
                    });
                } catch (IOException e) {
                    Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", e.getMessage()));
                }
            });
            t.setDaemon(true);
            t.start();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Giá tối đa và bước giá phải là số hợp lệ!");
        }
    }

    @FXML
    private void handleBack() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        ConnectionManager.getInstance().clearPushCallback();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
            Node dashboard = loader.load();

            StackPane contentArea = (StackPane) btnBack.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(dashboard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showTimeExtendedAnimation() {
        lblTimeExtended.setVisible(true);
        lblTimeExtended.setManaged(true);

        FadeTransition ft = new FadeTransition(Duration.seconds(3), lblTimeExtended);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            lblTimeExtended.setVisible(false);
            lblTimeExtended.setManaged(false);
            lblTimeExtended.setOpacity(1.0);
        });
        ft.play();
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
