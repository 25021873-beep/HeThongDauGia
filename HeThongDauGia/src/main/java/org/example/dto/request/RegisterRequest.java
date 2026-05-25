package org.example.dto.request;

public class RegisterRequest extends BaseRequest {
    private String username;
    private String password;
    private String role;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    @Override
    public String toString() {
        return "RegisterRequest{username='" + username + "', role='" + role + "', command='" + getCommand() + "'}";
    }
}