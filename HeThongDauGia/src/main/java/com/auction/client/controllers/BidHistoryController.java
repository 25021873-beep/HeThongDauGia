package com.auction.client.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class BidHistoryController {

    @FXML private TableView<String[]> tableBidHistory;
    @FXML private TableColumn<String[], String> colProductName;
    @FXML private TableColumn<String[], String> colBidAmount;
    @FXML private TableColumn<String[], String> colBidTime;
    @FXML private TableColumn<String[], String> colAuctionStatus;
    @FXML private TableColumn<String[], String> colResult;

    @FXML
    public void initialize() {
        //hien thi du lieu
        colProductName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[0]));
        colBidAmount.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[1]));
        colBidTime.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[2]));
        colAuctionStatus.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[3]));
        colResult.setCellValueFactory(data -> new SimpleStringProperty(data.getValue()[4]));

        //nap du lieu fake
        loadMockBidHistory();
    }

    private void loadMockBidHistory() {
        ObservableList<String[]> data = FXCollections.observableArrayList(
                new String[]{"Laptop Gaming ASUS", "15,000,000", "2025-05-10 14:30:25", "FINISHED", "🏆 Thắng"},
                new String[]{"iPhone 15 Pro Max", "28,500,000", "2025-05-10 15:12:10", "FINISHED", "❌ Thua"},
                new String[]{"Bức tranh sơn dầu cổ", "5,200,000", "2025-05-09 20:45:33", "RUNNING", "⏳ Đang đấu"},
                new String[]{"Xe Honda SH 150i", "45,000,000", "2025-05-09 18:20:15", "FINISHED", "❌ Thua"},
                new String[]{"Đồng hồ Rolex Vintage", "120,000,000", "2025-05-08 10:05:50", "FINISHED", "🏆 Thắng"},
                new String[]{"Camera Sony A7IV", "32,000,000", "2025-05-08 09:30:00", "RUNNING", "⏳ Đang đấu"},
                new String[]{"Bộ sưu tập tem cổ", "8,500,000", "2025-05-07 16:45:20", "CANCELED", "⛔ Hủy"}
        );
        tableBidHistory.setItems(data);
    }
}
