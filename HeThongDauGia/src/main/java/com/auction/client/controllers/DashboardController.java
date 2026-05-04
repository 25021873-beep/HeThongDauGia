package com.auction.client.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class DashboardController {

    @FXML
    private FlowPane productGridPane;

    @FXML
    public void initialize() {
        // Tải danh sách sản phẩm giả lập khi vừa mở màn hình
        loadMockProducts();
    }

    private void loadMockProducts() {
        // Tạo 6 sản phẩm giả để test giao diện lưới
        for (int i = 1; i <= 6; i++) {
            VBox productCard = createProductCard(
                    "Sản phẩm Đấu giá " + i,
                    "Mô tả ngắn gọn về sản phẩm số " + i + " đang được đấu giá vô cùng hấp dẫn.",
                    1500000.0 * i,
                    "RUNNING",
                    "00:15:" + (10 * i)
            );
            productGridPane.getChildren().add(productCard);
        }
    }

    /**
     * Hàm tự động vẽ một thẻ sản phẩm (Product Card) bằng Java code
     */
    private VBox createProductCard(String name, String description, double currentPrice, String status, String countdown) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5);");
        card.setPrefWidth(240); // Độ rộng cố định cho mỗi thẻ

        // Tên sản phẩm
        Label lblName = new Label(name);
        lblName.setFont(Font.font("System", FontWeight.BOLD, 16));
        lblName.setWrapText(true);

        // Mô tả sản phẩm
        Label lblDesc = new Label(description);
        lblDesc.setWrapText(true);
        lblDesc.setTextFill(Color.GRAY);

        // Trạng thái
        Label lblStatus = new Label("Trạng thái: " + status);
        lblStatus.setTextFill(Color.GREEN);
        lblStatus.setFont(Font.font("System", FontWeight.BOLD, 12));

        // Giá hiện tại
        Label lblPrice = new Label(String.format("Giá: %,.0f VNĐ", currentPrice));
        lblPrice.setTextFill(Color.RED);
        lblPrice.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Thời gian đếm ngược
        Label lblTime = new Label("Còn lại: " + countdown);
        lblTime.setStyle("-fx-text-fill: #E67E22; -fx-font-weight: bold;");

        // Nút bấm
        Button btnAction = new Button("Xem chi tiết / Đấu giá");
        btnAction.setMaxWidth(Double.MAX_VALUE); // Cho nút giãn hết chiều ngang của thẻ
        btnAction.setStyle("-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

        // Sự kiện khi bấm nút (Sau này sẽ chuyển sang màn hình Chi tiết đấu giá)
        btnAction.setOnAction(e -> {
            System.out.println("Đang mở chi tiết sản phẩm: " + name);
            // Logic load màn hình AuctionDetail.fxml sẽ được viết ở đây
        });

        // Đưa tất cả vào thẻ
        card.getChildren().addAll(lblName, lblDesc, lblStatus, lblPrice, lblTime, btnAction);
        card.setAlignment(Pos.TOP_LEFT);

        return card;
    }
}