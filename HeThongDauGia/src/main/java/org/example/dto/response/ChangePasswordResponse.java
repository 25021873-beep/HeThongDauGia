package org.example.dto.response;

/**
 * Response trả về sau lệnh CHANGE_PASSWORD thành công.
 * Thất bại dùng SimpleResponse.error(message).
 *
 * Serialize format:
 *   "SUCCESS|Doi mat khau thanh cong|<username>"
 *
 * Ví dụ:
 *   "SUCCESS|Doi mat khau thanh cong|alice"
 */
public class ChangePasswordResponse extends BaseResponse {

    private final String username;

    public ChangePasswordResponse(String username) {
        super(STATUS_SUCCESS, "Doi mat khau thanh cong");
        this.username = username;
    }

    public String getUsername() { return username; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS  + DELIMITER
                + getMessage() + DELIMITER
                + username;
    }
}