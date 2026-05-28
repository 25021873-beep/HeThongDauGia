package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.request.TopUpRequest;
import org.example.dto.response.SimpleResponse;
import org.example.dto.response.TopUpResponse;
import org.example.entity.user.User;
import org.example.network.SessionContext;
import org.example.service.UserService;

/**
 * Xử lý các lệnh liên quan đến tài khoản người dùng (ngoài auth):
 *   TOP_UP
 *
 * Có thể mở rộng thêm: GET_PROFILE, UPDATE_INFO, v.v.
 */
public class UserController {

    private final SessionContext session;
    private final UserService    userService;
    private final Gson           gson;

    public UserController(SessionContext session, UserService userService, Gson gson) {
        this.session     = session;
        this.userService = userService;
        this.gson        = gson;
    }

    // ── TOP_UP ────────────────────────────────────────────────────────────────

    public void handleTopUp(JsonObject json) {
        if (!session.requireLogin()) return;
        TopUpRequest req = gson.fromJson(json, TopUpRequest.class);

        boolean ok = userService.topUpBalance(
                session.getCurrentUser().getId(), req.getAmount());

        if (ok) {
            // Reload user để lấy balance mới nhất từ DB
            User updated = userService.getUserProfile(session.getCurrentUser().getId());
            if (updated != null) session.setCurrentUser(updated);

            session.send(new TopUpResponse(
                    session.getCurrentUser().getId(),
                    req.getAmount(),
                    session.getCurrentUser().getBalance()));
        } else {
            session.send(SimpleResponse.error("Nap tien that bai"));
        }
    }
    public void handleGetAllUsers() {
        if (!session.requireLogin()) return;

        try {
            java.util.List<User> users = userService.getAllUsers();
            JsonObject response = new JsonObject();
            response.addProperty("status", "SUCCESS");
            
            com.google.gson.JsonArray usersArray = new com.google.gson.JsonArray();
            for (User u : users) {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", u.getUsername());
                // Chú ý: CSDL hiện tại chưa có fullname nên tạm thời dùng username hoặc mock name
                obj.addProperty("fullName", u.getUsername()); 
                obj.addProperty("email", u.getEmail());
                obj.addProperty("role", u.getRole());
                obj.addProperty("status", "Hoạt động"); // Tạm thời mặc định là hoạt động vì db chưa có trường trạng thái block
                usersArray.add(obj);
            }
            response.add("users", usersArray);
            session.sendRaw(response.toString());

        } catch (Exception e) {
            System.err.println("[USER_CTRL] Loi lay danh sach user: " + e.getMessage());
            session.send(SimpleResponse.error("Loi he thong khi lay danh sach user"));
        }
    }
}