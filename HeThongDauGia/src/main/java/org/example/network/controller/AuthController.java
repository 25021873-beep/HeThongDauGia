package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.dto.request.ChangePasswordRequest;
import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.response.*;
import org.example.entity.user.User;
import org.example.network.SessionContext;
import org.example.service.UserService;

/**
 * Xử lý các lệnh liên quan đến xác thực người dùng:
 *   LOGIN | REGISTER | LOGOUT | CHANGE_PASSWORD
 */
public class AuthController {

    private final SessionContext session;
    private final UserService    userService;
    private final Gson           gson;

    public AuthController(SessionContext session, UserService userService, Gson gson) {
        this.session     = session;
        this.userService = userService;
        this.gson        = gson;
    }

    // ── LOGIN ─────────────────────────────────────────────────────────────────

    public void handleLogin(JsonObject json) {
        LoginRequest req = gson.fromJson(json, LoginRequest.class);

        if (req.getUsername() == null || req.getPassword() == null) {
            session.send(SimpleResponse.error("Thieu username hoac password"));
            return;
        }
        if (session.isLoggedIn()) {
            session.send(SimpleResponse.error("Ban da dang nhap roi. Hay LOGOUT truoc."));
            return;
        }

        User user = userService.login(req.getUsername(), req.getPassword());
        if (user != null) {
            session.setCurrentUser(user);
            session.send(new LoginResponse(
                    user.getId(), user.getUsername(), user.getRole(), user.getBalance()));
        } else {
            session.send(SimpleResponse.error("Sai ten dang nhap hoac mat khau"));
        }
    }

    // ── REGISTER ──────────────────────────────────────────────────────────────

    public void handleRegister(JsonObject json) {
        RegisterRequest req = gson.fromJson(json, RegisterRequest.class);

        if (req.getUsername() == null || req.getPassword() == null) {
            session.send(SimpleResponse.error("Thieu thong tin dang ky"));
            return;
        }
        if (req.getRole() == null) req.setRole("BIDDER");

        boolean success = userService.register(req);
        if (success) {
            User newUser = userService.getUserByUsername(req.getUsername());
            if (newUser != null) {
                session.send(new RegisterResponse(
                        newUser.getId(), newUser.getUsername(), newUser.getRole()));
            } else {
                session.send(SimpleResponse.success("Dang ky thanh cong. Vui long dang nhap."));
            }
        } else {
            session.send(SimpleResponse.error("Ten dang nhap da ton tai"));
        }
    }

    // ── LOGOUT ────────────────────────────────────────────────────────────────

    public void handleLogout() {
        String name = session.isLoggedIn()
                ? session.getCurrentUser().getUsername()
                : "Guest";
        session.send(SimpleResponse.success("Dang xuat thanh cong. Tam biet " + name + "!"));
        session.clearUser();
    }

    // ── CHANGE_PASSWORD ───────────────────────────────────────────────────────

    public void handleChangePassword(JsonObject json) {
        if (!session.requireLogin()) return;
        ChangePasswordRequest req = gson.fromJson(json, ChangePasswordRequest.class);

        boolean ok = userService.changePassword(
                session.getCurrentUser().getUsername(),
                req.getOldPassword(),
                req.getNewPassword());

        if (ok) {
            session.send(new ChangePasswordResponse(session.getCurrentUser().getUsername()));
        } else {
            session.send(SimpleResponse.error("Mat khau cu khong chinh xac"));
        }
    }
}