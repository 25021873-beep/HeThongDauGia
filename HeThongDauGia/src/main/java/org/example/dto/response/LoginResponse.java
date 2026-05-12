package org.example.dto.response;

import java.math.BigDecimal;

/**
 * Response trả về sau lệnh LOGIN thành công.
 *
 * Serialize format:
 *   "SUCCESS|Dang nhap thanh cong|<userId>|<username>|<role>|<balance>"
 *
 * Ví dụ:
 *   "SUCCESS|Dang nhap thanh cong|5|alice|BIDDER|1500000"
 */
public class LoginResponse extends BaseResponse {

    private final int userId;
    private final String username;
    private final String role;
    private final BigDecimal balance;

    public LoginResponse(int userId, String username, String role, BigDecimal balance) {
        super(STATUS_SUCCESS, "Dang nhap thanh cong");
        this.userId   = userId;
        this.username = username;
        this.role     = role;
        this.balance  = balance != null ? balance : BigDecimal.ZERO;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int       getUserId()  { return userId; }
    public String    getUsername(){ return username; }
    public String    getRole()    { return role; }
    public BigDecimal getBalance(){ return balance; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS + DELIMITER
                + getMessage() + DELIMITER
                + userId       + DELIMITER
                + username     + DELIMITER
                + role         + DELIMITER
                + balance;
    }
}
