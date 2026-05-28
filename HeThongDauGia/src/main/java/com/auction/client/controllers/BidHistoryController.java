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

        //nap du lieu tu server
        loadBidHistoryFromServer();
    }

    private void loadBidHistoryFromServer() {
        Thread t = new Thread(() -> {
            try {
                com.auction.client.network.ConnectionManager conn = com.auction.client.network.ConnectionManager.getInstance();
                if (!conn.isConnected()) return;

                com.google.gson.JsonObject req = new com.google.gson.JsonObject();
                req.addProperty("command", "GET_USER_BID_HISTORY");

                com.google.gson.JsonObject res = conn.sendAndWait(req);

                javafx.application.Platform.runLater(() -> {
                    if (com.auction.client.network.ServerClient.isSuccess(res)) {
                        ObservableList<String[]> data = FXCollections.observableArrayList();
                        if (res.has("history")) {
                            res.getAsJsonArray("history").forEach(elem -> {
                                com.google.gson.JsonObject obj = elem.getAsJsonObject();
                                String name = obj.has("productName") ? obj.get("productName").getAsString() : "";
                                double price = obj.has("bidAmount") ? obj.get("bidAmount").getAsDouble() : 0;
                                String formattedPrice = String.format("%,.0f VNĐ", price);
                                String time = obj.has("bidTime") ? obj.get("bidTime").getAsString() : "";
                                
                                String status = obj.has("status") ? obj.get("status").getAsString() : "";
                                String displayStatus = "RUNNING".equals(status) ? "Đang diễn ra" : 
                                                       "OPEN".equals(status) ? "Sắp bắt đầu" : 
                                                       "CANCELED".equals(status) ? "Đã hủy" : "Đã kết thúc";
                                
                                String result = obj.has("result") ? obj.get("result").getAsString() : "Đang đấu";
                                String displayResult = "Thắng".equals(result) ? "🏆 Thắng" : 
                                                       "Thua".equals(result) ? "❌ Thua" : 
                                                       "Hủy".equals(result) ? "⛔ Hủy" : "⏳ Đang đấu";

                                data.add(new String[]{name, formattedPrice, time, displayStatus, displayResult});
                            });
                        }
                        tableBidHistory.setItems(data);
                    } else {
                        System.err.println("Loi: " + com.auction.client.network.ServerClient.messageOf(res));
                    }
                });
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        });
        t.setDaemon(true);
        t.start();
    }
}
