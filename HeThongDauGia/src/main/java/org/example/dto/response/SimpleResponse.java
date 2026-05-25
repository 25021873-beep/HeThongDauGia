package org.example.dto.response;

/**
 * Response đơn giản không có data field.
 * Dùng cho: ERROR, INFO, LOGOUT, thông báo chung.
 *
 * JSON output:
 * {"status":"ERROR","message":"Ban chua dang nhap"}
 */
public class SimpleResponse extends BaseResponse {

    public SimpleResponse(String status, String message) {
        super(status, message);
    }

    public static SimpleResponse success(String message) {
        return new SimpleResponse(STATUS_SUCCESS, message);
    }

    public static SimpleResponse error(String message) {
        return new SimpleResponse(STATUS_ERROR, message);
    }

    public static SimpleResponse info(String message) {
        return new SimpleResponse(STATUS_INFO, message);
    }
}