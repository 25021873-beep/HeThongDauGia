package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
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

    @FXML
    public void initialize() {
        //tai spham fake
        loadMockProducts();

        //thiet lap search
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> {
                filterProducts(newVal);
            });
        }
    }

    private void loadMockProducts() {
        productGridPane.getChildren().clear();

        //tao spham fake
        String[][] products = {
                {"Laptop Gaming ASUS ROG Strix", "Laptop gaming cao cấp RTX 4070, RAM 16GB", "15000000", "RUNNING", "15:30", "Điện tử", "seller01"},
                {"iPhone 15 Pro Max 256GB", "iPhone chính hãng, fullbox, mới 100%", "28500000", "RUNNING", "22:45", "Điện tử", "seller02"},
                {"Bức tranh sơn dầu phong cảnh", "Tranh sơn dầu vẽ tay, phong cảnh Hà Nội cổ", "5200000", "RUNNING", "08:20", "Nghệ thuật", "seller01"},
                {"Honda SH 150i ABS 2024", "Xe SH 150i mới, đủ màu, bảo hành 3 năm", "45000000", "RUNNING", "12:00", "Xe cộ", "seller03"},
                {"Đồng hồ Rolex Submariner", "Rolex Submariner Date, vintage 1985", "120000000", "RUNNING", "06:15", "Đồ cổ", "seller02"},
                {"Camera Sony A7IV Body", "Máy ảnh mirrorless Sony A7IV, fullframe", "32000000", "OPEN", "30:00", "Điện tử", "seller01"},
                {"Bộ sưu tập tem cổ Đông Dương", "Bộ tem quý hiếm thời Đông Dương, 50 con", "8500000", "RUNNING", "18:40", "Đồ cổ", "seller03"},
                {"MacBook Pro M3 14 inch", "MacBook Pro chip M3 Pro, RAM 18GB, SSD 512GB", "38000000", "OPEN", "45:00", "Điện tử", "seller02"},
        };

        for (String[] product : products) {
            VBox productCard = createProductCard(
                    product[0], product[1],
                    Double.parseDouble(product[2]),
                    product[3], product[4],
                    product[5], product[6]
            );
            productGridPane.getChildren().add(productCard);
        }
    }

    private void filterProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            loadMockProducts();
            return;
        }

        String lower = keyword.toLowerCase();
        productGridPane.getChildren().removeIf(node -> {
            if (node instanceof VBox) {
                VBox card = (VBox) node;
                // The name label is the second child (index 1), after category badge
                if (card.getChildren().size() > 1) {
                    Label nameLabel = (Label) card.getChildren().get(1);
                    return !nameLabel.getText().toLowerCase().contains(lower);
                }
            }
            return false;
        });
    }

//ve the spham bang javacode
    private VBox createProductCard(String name, String description, double currentPrice,
                                    String status, String countdown, String category, String seller) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: rgba(30,30,30,0.45); -fx-background-radius: 12; "
                + "-fx-border-color: rgba(245,125,31,0.12); -fx-border-radius: 12; -fx-border-width: 1;");
        card.setPrefWidth(255);

        //badge
        Label lblCategory = new Label(category);
        lblCategory.setStyle("-fx-background-color: #1A1A1A; -fx-text-fill: #FCBF49; -fx-padding: 4 12; "
                + "-fx-background-radius: 12; -fx-font-size: 11px; -fx-font-weight: bold;");

        //name
        Label lblName = new Label(name);
        lblName.setFont(Font.font("System", FontWeight.BOLD, 15));
        lblName.setTextFill(Color.web("#FFFFFF"));
        lblName.setWrapText(true);
        lblName.setMaxHeight(40);

        //des
        Label lblDesc = new Label(description);
        lblDesc.setWrapText(true);
        lblDesc.setTextFill(Color.web("#AAAAAA"));
        lblDesc.setMaxHeight(35);
        lblDesc.setStyle("-fx-font-size: 12px;");

        //status
        Label lblStatus = new Label(status.equals("RUNNING") ? "🟢 Đang diễn ra" : "🟡 Sắp bắt đầu");
        lblStatus.setTextFill(status.equals("RUNNING") ? Color.web("#2E8B57") : Color.web("#F57D1F"));
        lblStatus.setFont(Font.font("System", FontWeight.BOLD, 12));

        //current price
        Label lblPrice = new Label(String.format("💰 %,.0f VNĐ", currentPrice));
        lblPrice.setTextFill(Color.web("#F57D1F"));
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 14));

        //countdown time
        Label lblTime = new Label("⏱ Còn lại: " + countdown);
        lblTime.setStyle("-fx-text-fill: #FCBF49; -fx-font-weight: bold; -fx-font-size: 12px; "
                + "-fx-background-color: #1A1A1A; -fx-padding: 3 10; -fx-background-radius: 6;");

        //button
        Button btnAction = new Button("🔍 Xem chi tiết / Đấu giá");
        btnAction.setMaxWidth(Double.MAX_VALUE);
        btnAction.setStyle("-fx-background-color: linear-gradient(to right, #F57D1F, #FCBF49); "
                + "-fx-text-fill: #1A1A1A; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8;");
        btnAction.setPadding(new Insets(8, 15, 8, 15));

        //bam nut chuyen sang auction detail
        btnAction.setOnAction(e -> {
            openAuctionDetail(name, description, currentPrice, category, seller);
        });

        //dua vao card
        card.getChildren().addAll(lblCategory, lblName, lblDesc, lblStatus, lblPrice, lblTime, btnAction);
        card.setAlignment(Pos.TOP_LEFT);

        return card;
    }

    //man hinh chi tiet dau gia
    private void openAuctionDetail(String name, String description, double price, String category, String seller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AuctionDetail.fxml"));
            Node detailNode = loader.load();

            //truyen ttin sang auction detail controller
            AuctionDetailController controller = loader.getController();
            controller.setProductInfo(name, description, price, category, seller);

            //tim content area trong mainlayout
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
}