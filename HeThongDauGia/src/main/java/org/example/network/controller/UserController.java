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
}