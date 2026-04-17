package com.auction.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Lệnh này sẽ đi tìm file Login.fxml trong thư mục resources để nạp lên giao diện
        Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/Login.fxml")));

        // Nhét bộ khung giao diện vào Scene
        Scene scene = new Scene(root);

        primaryStage.setTitle("Hệ thống Đấu giá trực tuyến - Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Khóa người dùng kéo giãn màn hình lung tung
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}