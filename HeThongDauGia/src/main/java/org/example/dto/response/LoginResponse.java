package org.example.dto.response;

import java.math.BigDecimal;

/**
 * JSON output:
 * {"status":"SUCCESS","message":"Dang nhap thanh cong",
 *  "userId":5,"username":"alice","role":"BIDDER","balance":1500000}
 */
public class LoginResponse extends BaseResponse {

    private final int        userId;
    private final String     username;
    private final String     role;
    private final BigDecimal balance;

    public LoginResponse(int userId, String username, String role, BigDecimal balance) {
        super(STATUS_SUCCESS, "Dang nhap thanh cong");
        this.userId   = userId;
        this.username = username;
        this.role     = role;
        this.balance  = balance != null ? balance : BigDecimal.ZERO;
    }

    public int        getUserId()  { return userId; }
    public String     getUsername(){ return username; }
    public String     getRole()    { return role; }
    public BigDecimal getBalance() { return balance; }
}