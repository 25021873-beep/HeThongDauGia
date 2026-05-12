package org.example.dto.response;

/**
 * Response đơn giản chỉ có status + message.
 * Dùng cho: ERROR, INFO, LOGOUT, CHANGE_PASSWORD, TOP_UP.
 *
 * Ví dụ serialize:
 *   "ERROR|Sai ten dang nhap hoac mat khau"
 *   "SUCCESS|Dang xuat thanh cong"
 */
public class SimpleResponse extends BaseResponse {

    public SimpleResponse(String status, String message) {
        super(status, message);
    }

    // ── Static factories tiện dụng ───────────────────────────────────────────

    public static SimpleResponse success(String message) {
        return new SimpleResponse(STATUS_SUCCESS, message);
    }

    public static SimpleResponse error(String message) {
        return new SimpleResponse(STATUS_ERROR, message);
    }

    public static SimpleResponse info(String message) {
        return new SimpleResponse(STATUS_INFO, message);
    }

    // serialize() dùng mặc định của BaseResponse: "STATUS|message"
}