package com.auction.client.controllers;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton quản lý Toast Notifications.
 * Hiển thị popup ở góc dưới bên phải màn hình, tự động fade out,
 * có nút X để đóng ngay.
 */
public class ToastManager {

    private static final int MAX_TOASTS = 5;
    private static final double TOAST_WIDTH = 380;
    private static final double TOAST_MARGIN_BOTTOM = 20;
    private static final double TOAST_MARGIN_RIGHT = 20;
    private static final double TOAST_SPACING = 8;
    private static final double TOAST_HEIGHT_ESTIMATE = 70;
    private static final int DEFAULT_DURATION_MS = 5000;

    // Kiểu toast
    public enum ToastType {
        INFO,       // Xanh lá — thành công
        ERROR,      // Đỏ — lỗi
        WARNING,    // Vàng — cảnh báo
        BID         // Cam — có người bid
    }

    // Danh sách popup đang hiển thị
    private static final List<Popup> activePopups = new ArrayList<>();

    // ═══════════════ PUBLIC API ═══════════════

    /**
     * Hiển thị thông báo thành công.
     */
    public static void showInfo(String message) {
        show(ToastType.INFO, message, DEFAULT_DURATION_MS);
    }

    /**
     * Hiển thị thông báo lỗi.
     */
    public static void showError(String message) {
        show(ToastType.ERROR, message, DEFAULT_DURATION_MS);
    }

    /**
     * Hiển thị thông báo cảnh báo.
     */
    public static void showWarning(String message) {
        show(ToastType.WARNING, message, DEFAULT_DURATION_MS);
    }

    /**
     * Hiển thị thông báo có người bid vào phiên đấu giá.
     */
    public static void showBidNotification(String productName, String bidder, double amount) {
        String msg = "🔔 Phiên \"" + productName + "\"\n"
                + bidder + " vừa đặt giá " + String.format("%,.0f VNĐ", amount);
        show(ToastType.BID, msg, DEFAULT_DURATION_MS + 2000); // hiển thị lâu hơn 1 chút
    }

    // ═══════════════ INTERNAL ═══════════════

    private static void show(ToastType type, String message, int durationMs) {
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(type, message, durationMs));
            return;
        }

        Window owner = getActiveWindow();
        if (owner == null) return;

        // Giới hạn số lượng toast
        while (activePopups.size() >= MAX_TOASTS) {
            Popup oldest = activePopups.remove(0);
            oldest.hide();
        }

        Popup popup = new Popup();
        popup.setAutoHide(false);

        // ── Xây dựng nội dung toast ──

        // Icon
        String icon;
        String bgColor;
        String borderColor;
        String iconBgColor;

        switch (type) {
            case ERROR:
                icon = "✕";
                bgColor = "rgba(30, 30, 30, 0.95)";
                borderColor = "rgba(192, 57, 43, 0.6)";
                iconBgColor = "#C0392B";
                break;
            case WARNING:
                icon = "⚠";
                bgColor = "rgba(30, 30, 30, 0.95)";
                borderColor = "rgba(252, 191, 73, 0.6)";
                iconBgColor = "#FCBF49";
                break;
            case BID:
                icon = "🔨";
                bgColor = "rgba(30, 30, 30, 0.95)";
                borderColor = "rgba(245, 125, 31, 0.6)";
                iconBgColor = "#F57D1F";
                break;
            default: // INFO
                icon = "✓";
                bgColor = "rgba(30, 30, 30, 0.95)";
                borderColor = "rgba(46, 139, 87, 0.6)";
                iconBgColor = "#2E8B57";
                break;
        }

        // Icon label
        Label lblIcon = new Label(icon);
        lblIcon.setStyle(
                "-fx-background-color: " + iconBgColor + ";"
                + "-fx-background-radius: 50;"
                + "-fx-min-width: 32; -fx-min-height: 32;"
                + "-fx-max-width: 32; -fx-max-height: 32;"
                + "-fx-alignment: center;"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 14px;"
                + "-fx-font-weight: bold;"
        );

        // Message label
        Label lblMessage = new Label(message);
        lblMessage.setWrapText(true);
        lblMessage.setMaxWidth(TOAST_WIDTH - 100);
        lblMessage.setStyle(
                "-fx-text-fill: #EEEEEE;"
                + "-fx-font-size: 13px;"
        );

        // Nút X đóng
        Button btnClose = new Button("✕");
        btnClose.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-text-fill: #888888;"
                + "-fx-font-size: 14px;"
                + "-fx-cursor: hand;"
                + "-fx-padding: 0 4 0 4;"
                + "-fx-min-width: 24; -fx-min-height: 24;"
        );
        btnClose.setOnMouseEntered(e -> btnClose.setStyle(
                "-fx-background-color: rgba(255,255,255,0.1);"
                + "-fx-background-radius: 50;"
                + "-fx-text-fill: #FFFFFF;"
                + "-fx-font-size: 14px;"
                + "-fx-cursor: hand;"
                + "-fx-padding: 0 4 0 4;"
                + "-fx-min-width: 24; -fx-min-height: 24;"
        ));
        btnClose.setOnMouseExited(e -> btnClose.setStyle(
                "-fx-background-color: transparent;"
                + "-fx-text-fill: #888888;"
                + "-fx-font-size: 14px;"
                + "-fx-cursor: hand;"
                + "-fx-padding: 0 4 0 4;"
                + "-fx-min-width: 24; -fx-min-height: 24;"
        ));
        btnClose.setOnAction(e -> dismissToast(popup));

        // Spacer giữa message và nút X
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Layout ngang: Icon | Message | spacer | X
        HBox content = new HBox(12, lblIcon, lblMessage, spacer, btnClose);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(14, 16, 14, 16));
        content.setPrefWidth(TOAST_WIDTH);
        content.setMaxWidth(TOAST_WIDTH);
        content.setStyle(
                "-fx-background-color: " + bgColor + ";"
                + "-fx-background-radius: 12;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-radius: 12;"
                + "-fx-border-width: 1;"
                + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 12, 0, 0, 4);"
        );

        popup.getContent().add(content);

        // ── Hiển thị và animation ──

        // Thêm vào danh sách trước khi show để tính vị trí
        activePopups.add(popup);

        // Tính vị trí: góc dưới bên phải
        repositionAllToasts(owner);

        popup.show(owner);

        // Opacity ban đầu = 0, animate vào
        content.setOpacity(0);
        content.setTranslateX(30);

        // Slide in + fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), content);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), content);
        slideIn.setFromX(30);
        slideIn.setToX(0);

        ParallelTransition enterAnim = new ParallelTransition(fadeIn, slideIn);
        enterAnim.play();

        // Timer tự động đóng
        PauseTransition pause = new PauseTransition(Duration.millis(durationMs));
        pause.setOnFinished(e -> dismissToast(popup));
        pause.play();

        // Lưu reference để cancel nếu đóng sớm
        popup.setUserData(pause);
    }

    /**
     * Đóng toast với animation fade out.
     */
    private static void dismissToast(Popup popup) {
        if (!activePopups.contains(popup)) return;

        if (popup.getContent().isEmpty()) {
            activePopups.remove(popup);
            popup.hide();
            return;
        }

        // Cancel timer nếu còn
        if (popup.getUserData() instanceof PauseTransition) {
            ((PauseTransition) popup.getUserData()).stop();
        }

        javafx.scene.Node content = popup.getContent().get(0);

        // Fade out + slide out
        FadeTransition fadeOut = new FadeTransition(Duration.millis(400), content);
        fadeOut.setFromValue(content.getOpacity());
        fadeOut.setToValue(0);

        TranslateTransition slideOut = new TranslateTransition(Duration.millis(400), content);
        slideOut.setFromX(0);
        slideOut.setToX(30);

        ParallelTransition exitAnim = new ParallelTransition(fadeOut, slideOut);
        exitAnim.setOnFinished(e -> {
            activePopups.remove(popup);
            popup.hide();
            // Reposition remaining toasts
            Window owner = getActiveWindow();
            if (owner != null) {
                repositionAllToasts(owner);
            }
        });
        exitAnim.play();
    }

    /**
     * Tính lại vị trí tất cả toast (stack từ dưới lên).
     */
    private static void repositionAllToasts(Window owner) {
        double baseX = owner.getX() + owner.getWidth() - TOAST_WIDTH - TOAST_MARGIN_RIGHT;
        double baseY = owner.getY() + owner.getHeight() - TOAST_MARGIN_BOTTOM;

        for (int i = activePopups.size() - 1; i >= 0; i--) {
            Popup p = activePopups.get(i);
            int indexFromBottom = activePopups.size() - 1 - i;
            double y = baseY - (indexFromBottom + 1) * (TOAST_HEIGHT_ESTIMATE + TOAST_SPACING);

            p.setX(baseX);
            p.setY(y);
        }
    }

    /**
     * Lấy cửa sổ đang active.
     */
    private static Window getActiveWindow() {
        // Ưu tiên focused window, fallback sang window đầu tiên đang hiện
        for (Window w : Stage.getWindows()) {
            if (w instanceof Stage && ((Stage) w).isFocused()) {
                return w;
            }
        }
        for (Window w : Stage.getWindows()) {
            if (w.isShowing()) {
                return w;
            }
        }
        return null;
    }
}
