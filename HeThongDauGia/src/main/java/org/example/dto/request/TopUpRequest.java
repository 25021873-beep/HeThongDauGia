package org.example.dto.request;

import java.math.BigDecimal;

public class TopUpRequest extends BaseRequest {
    private int userId;
    private BigDecimal amount;

    public TopUpRequest() {}

    public TopUpRequest(int userId, BigDecimal amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    @Override
    public String toString() {
        return "TopUpRequest{userId=" + userId + ", amount=" + amount + ", command='" + getCommand() + "'}";
    }
}