package org.example.dto.response;

import java.math.BigDecimal;

/**
 * JSON output:
 * {"status":"SUCCESS","message":"Nap tien thanh cong",
 *  "userId":5,"amountAdded":500000,"newBalance":2000000}
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

    public int        getUserId()      { return userId; }
    public BigDecimal getAmountAdded() { return amountAdded; }
    public BigDecimal getNewBalance()  { return newBalance; }
}