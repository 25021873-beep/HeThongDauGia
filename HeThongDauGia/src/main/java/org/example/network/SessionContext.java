package org.example.network;

import com.google.gson.Gson;
import org.example.dto.response.BaseResponse;
import org.example.dto.response.SimpleResponse;
import org.example.entity.user.User;

import java.io.PrintWriter;

/**
 * Trạng thái dùng chung giữa ClientHandler và các controller.
 *
 * Đóng gói:
 *   - currentUser (ai đang đăng nhập trong session này)
 *   - send()      (cách gửi response về client)
 *
 * Được inject vào tất cả controller — controller không cần
 * biết ClientHandler là gì, chỉ cần SessionContext.
 */
public class SessionContext {

    private User        currentUser;
    private PrintWriter out;
    private final Gson  gson;

    public SessionContext(PrintWriter out, Gson gson) {
        this.out  = out;
        this.gson = gson;
    }

    // ── Send ──────────────────────────────────────────────────────────────────

    /** Gửi response về client dưới dạng JSON. */
    public void send(BaseResponse response) {
        if (out != null) out.println(gson.toJson(response));
    }

    /** Gửi chuỗi thô — dùng khi đã serialize sẵn (ví dụ AuctionResultResponse). */
    public void sendRaw(String json) {
        if (out != null) out.println(json);
    }

    // ── Auth guard ────────────────────────────────────────────────────────────

    /**
     * Kiểm tra user đã đăng nhập chưa.
     * Nếu chưa → tự gửi lỗi và trả về false.
     */
    public boolean requireLogin() {
        if (currentUser == null) {
            send(SimpleResponse.error("Ban chua dang nhap"));
            return false;
        }
        return true;
    }

    // ── Getters / Setters ─────────────────────────────────────────────────────

    public User getCurrentUser()            { return currentUser; }
    public void setCurrentUser(User user)   { this.currentUser = user; }
    public boolean isLoggedIn()             { return currentUser != null; }
    public void    clearUser()              { this.currentUser = null; }
}