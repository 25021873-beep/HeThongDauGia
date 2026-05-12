package org.example.dto.response;

/**
 * Response trả về sau lệnh REGISTER thành công.
 *
 * Serialize format:
 *   "SUCCESS|Dang ky thanh cong|<userId>|<username>|<role>"
 *
 * Ví dụ:
 *   "SUCCESS|Dang ky thanh cong|7|bob|BIDDER"
 */
public class RegisterResponse extends BaseResponse {

    private final int    userId;
    private final String username;
    private final String role;

    public RegisterResponse(int userId, String username, String role) {
        super(STATUS_SUCCESS, "Dang ky thanh cong");
        this.userId   = userId;
        this.username = username;
        this.role     = role;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int    getUserId()  { return userId; }
    public String getUsername(){ return username; }
    public String getRole()    { return role; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS + DELIMITER
                + getMessage() + DELIMITER
                + userId       + DELIMITER
                + username     + DELIMITER
                + role;
    }
}