package com.auction.client.controllers;

import com.auction.client.network.ConnectionManager;
import com.auction.client.network.ServerClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.IOException;

public class DashboardController {

    @FXML
    private FlowPane productGridPane;

    @FXML
    private TextField txtSearch;

    private JsonArray allAuctions = new JsonArray();

    @FXML
    public void initialize() {
        // Tải danh sách phiên đấu giá từ Server
        loadAuctionsFromServer();

        // Thiết lập tìm kiếm
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filterProducts(newVal);
            });
        }
    }

    private void loadAuctionsFromServer() {
        Thread thread = new Thread(() -> {
            try {
                ConnectionManager conn = ConnectionManager.getInstance();
                if (!conn.isConnected()) return;

                JsonObject request = new JsonObject();
                request.addProperty("command", "GET_ALL_AUCTIONS");

                JsonObject response = conn.sendAndWait(request);

                Platform.runLater(() -> {
                    if (ServerClient.isSuccess(response) || "LIST_SUCCESS".equals(response.get("status").getAsString())) {
                        if (response.has("auctions")) {
                            allAuctions = response.getAsJsonArray("auctions");
                            displayAuctions(allAuctions);
                        } else {
                            productGridPane.getChildren().clear();
                        }
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Lỗi", ServerClient.messageOf(response));
                    }
                });

            } catch (IOException e) {
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể lấy danh sách phiên đấu giá."));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private void displayAuctions(JsonArray auctions) {
        productGridPane.getChildren().clear();
        for (JsonElement elem : auctions) {
            JsonObject auction = elem.getAsJsonObject();
            int id = auction.has("id") ? auction.get("id").getAsInt() : 0;
            String name = auction.has("name") ? auction.get("name").getAsString() : "Chưa có tên";
            double currentPrice = auction.has("currentPrice") ? auction.get("currentPrice").getAsDouble() : 0;
            String status = auction.has("status") ? auction.get("status").getAsString() : "OPEN";

            // Lọc bỏ phiên đã kết thúc hoặc hủy — Dashboard chỉ hiện phiên đang mở
            if ("FINISHED".equals(status) || "CANCELED".equals(status)) {
                continue;
            }

            // Giả lập description, countdown, category, seller vì list summary chưa có đủ
            String description = "Chi tiết phiên đấu giá " + id;
            String countdown = "N/A";
            String category = "Sản phẩm";
            String seller = "Người bán";

            VBox productCard = createProductCard(
                    id, name, description, currentPrice, status, countdown, category, seller
            );
            productGridPane.getChildren().add(productCard);
        }
    }

    private void filterProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            displayAuctions(allAuctions);
            return;
        }

        String lower = keyword.toLowerCase();
        JsonArray filtered = new JsonArray();
        for (JsonElement elem : allAuctions) {
            JsonObject auction = elem.getAsJsonObject();
            String name = auction.has("name") ? auction.get("name").getAsString() : "";
            if (name.toLowerCase().contains(lower)) {
                filtered.add(elem);
            }
        }
        displayAuctions(filtered);
    }

    private VBox createProductCard(int id, String name, String description, double currentPrice,
                                    String status, String countdown, String category, String seller) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: rgba(30,30,30,0.45); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(245,125,31,0.12); -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefWidth(255);

        // badge
        Label lblCategory = new Label(category);
        lblCategory.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: #FCBF49; -fx-padding: 4 12; "
                + "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        // name
        Label lblName = new Label(name);
        lblName.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblName.setTextFill(Color.web("#FFFFFF"));
        lblName.setWrapText(true);
        lblName.setMaxHeight(40);

        // status - xử lý đúng tất cả trạng thái từ server
        String displayStatus;
        Color statusColor;
        if ("RUNNING".equals(status)) {
            displayStatus = "● Đang diễn ra";
            statusColor = Color.web("#2E8B57");
        } else if ("OPEN".equals(status)) {
            displayStatus = "● Sắp bắt đầu";
            statusColor = Color.web("#F57D1F");
        } else {
            displayStatus = "● Đã kết thúc";
            statusColor = Color.web("#888888");
        }
        Label lblStatus = new Label(displayStatus);
        lblStatus.setTextFill(statusColor);
        lblStatus.setFont(Font.font("System", FontWeight.BOLD, 12));

        // current price
        Label lblPrice = new Label(String.format("💰 %,.0f VNĐ", currentPrice));
        lblPrice.setTextFill(Color.web("#F57D1F"));
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 14));

        // button
        Button btnAction = new Button("🔍 Xem chi tiết / Đấu giá");
        btnAction.setMaxWidth(Double.MAX_VALUE);
        btnAction.setStyle("-fx-background-color: linear-gradient(to right, #F57D1F, #FCBF49); "
                + "-fx-text-fill: #1A1A1A; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAction.setPadding(new Insets(8, 15, 8, 15));

        // Bấm nút chuyển sang auction detail
        btnAction.setOnAction(e -> {
            openAuctionDetail(id, name, description, currentPrice, category, seller);
        });

        // Đưa vào card
        card.getChildren().addAll(lblCategory, lblName, lblStatus, lblPrice, btnAction);
        card.setAlignment(Pos.TOP_LEFT);

        return card;
    }

    private void openAuctionDetail(int auctionId, String name, String description, double price, String category, String seller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionDetail.fxml"));
            Node detailNode = loader.load();

            AuctionDetailController controller = loader.getController();
            controller.setProductInfo(auctionId, name, description, price, category, seller);

            StackPane contentArea = (StackPane) productGridPane.getScene().lookup("#contentArea");
            if (contentArea != null) {
                contentArea.getChildren().clear();
                contentArea.getChildren().add(detailNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Không thể mở màn hình chi tiết đấu giá: " + e.getMessage());
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