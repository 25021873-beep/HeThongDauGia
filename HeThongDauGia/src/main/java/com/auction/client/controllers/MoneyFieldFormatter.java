package com.auction.client.controllers;

import javafx.scene.control.TextField;

/**
 * Tiện ích tự động thêm dấu phẩy phân cách hàng nghìn khi người dùng nhập số tiền.
 * Ví dụ: 1000000 → 1,000,000
 */
public class MoneyFieldFormatter {

    /**
     * Gắn listener tự động format cho TextField.
     * Chỉ cho phép nhập số, tự động thêm dấu phẩy.
     */
    public static void apply(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;

            // Lấy phần chữ số, bỏ mọi ký tự khác
            String digits = newVal.replaceAll("[^\\d]", "");
            if (digits.isEmpty()) {
                textField.setText("");
                return;
            }

            // Format với dấu phẩy
            String formatted = formatWithCommas(digits);
            if (!formatted.equals(newVal)) {
                textField.setText(formatted);
                textField.positionCaret(formatted.length());
            }
        });
    }

    /**
     * Lấy giá trị số thuần (không dấu phẩy) từ TextField đã format.
     */
    public static String getRawValue(TextField textField) {
        String text = textField.getText();
        if (text == null) return "";
        return text.replaceAll("[^\\d]", "");
    }

    private static String formatWithCommas(String digits) {
        // Bỏ số 0 đầu
        digits = digits.replaceFirst("^0+", "");
        if (digits.isEmpty()) return "0";

        StringBuilder sb = new StringBuilder();
        int len = digits.length();
        for (int i = 0; i < len; i++) {
            if (i > 0 && (len - i) % 3 == 0) {
                sb.append(',');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }
}
