package org.example.dto.response;

import java.math.BigDecimal;

/**
 * Response trả về sau lệnh TOP_UP thành công.
 *
 * Serialize format:
 *   "SUCCESS|Nap tien thanh cong|<userId>|<amountAdded>|<newBalance>"
 *
 * Ví dụ:
 *   "SUCCESS|Nap tien thanh cong|5|500000|2000000"
 */
public class TopUpResponse extends BaseResponse {

    private final int        userId;
    private final BigDecimal amountAdded;
    private final BigDecimal newBalance;

    public TopUpResponse(int userId, BigDecimal amountAdded, BigDecimal newBalance) {
        super(STATUS_SUCCESS, "Nap tien thanh cong");
        this.userId      = userId;
        this.amountAdded = amountAdded;
        this.newBalance  = newBalance;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public int        getUserId()     { return userId; }
    public BigDecimal getAmountAdded(){ return amountAdded; }
    public BigDecimal getNewBalance() { return newBalance; }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        return STATUS_SUCCESS  + DELIMITER
                + getMessage() + DELIMITER
                + userId       + DELIMITER
                + amountAdded  + DELIMITER
                + newBalance;
    }
}