package org.example.dto.response;

/**
 * JSON output:
 * {"status":"SUCCESS","message":"Dang ky thanh cong",
 *  "userId":7,"username":"bob","role":"BIDDER"}
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

    public int    getUserId()  { return userId; }
    public String getUsername(){ return username; }
    public String getRole()    { return role; }
}