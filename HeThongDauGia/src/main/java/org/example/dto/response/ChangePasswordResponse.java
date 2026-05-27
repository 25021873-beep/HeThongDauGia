package org.example.dto.response;

/**
 * JSON output:
 * {"status":"SUCCESS","message":"Doi mat khau thanh cong","username":"alice"}
 */
public class ChangePasswordResponse extends BaseResponse {

    private final String username;

    public ChangePasswordResponse(String username) {
        super(STATUS_SUCCESS, "Doi mat khau thanh cong");
        this.username = username;
    }

    public String getUsername() { return username; }
}