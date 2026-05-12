package org.example.dto.response;


/**
 * Wrapper gốc cho mọi response gửi về client qua Socket.
 *
 * Protocol text format:
 *   SUCCESS|<message>|<data_fields...>
 *   ERROR|<message>
 *
 * Mỗi subclass override serialize() để tạo ra chuỗi phù hợp với lệnh của nó.
 */
public abstract class BaseResponse {

    public static final String STATUS_SUCCESS = "SUCCESS";
    public static final String STATUS_ERROR   = "ERROR";
    public static final String STATUS_INFO    = "INFO";
    public static final String DELIMITER      = "|";

    private final String status;
    private final String message;

    protected BaseResponse(String status, String message) {
        this.status  = status;
        this.message = message;
    }

    // ── Static factories dùng chung ──────────────────────────────────────────

    /** Tạo response lỗi đơn giản, không cần subclass */
    public static SimpleResponse error(String message) {
        return new SimpleResponse(STATUS_ERROR, message);
    }

    /** Tạo response thông tin đơn giản (INFO) */
    public static SimpleResponse info(String message) {
        return new SimpleResponse(STATUS_INFO, message);
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getStatus()  { return status; }
    public String getMessage() { return message; }
    public boolean isSuccess() { return STATUS_SUCCESS.equals(status); }

    /**
     * Serialize thành chuỗi gửi qua socket.
     * Subclass override để thêm data fields sau message.
     * Mặc định: "STATUS|message"
     */
    public String serialize() {
        return status + DELIMITER + message;
    }

    @Override
    public String toString() {
        return serialize();
    }
}
