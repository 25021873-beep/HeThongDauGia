package org.example.service;

import org.example.dao.user.UserDAO;
import org.example.dto.request.RegisterRequest;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    // ── Đăng nhập ─────────────────────────────────────────────────────────────


    public User login(String username, String password) {
        User existingUser = userDAO.getUserByUsername(username);
        if (existingUser == null) return null;

        if (BCrypt.checkpw(password, existingUser.getPassword())) {
            System.out.println("[AUTH] Dang nhap thanh cong: " + username);
            return existingUser;
        } else {
            System.out.println("[AUTH] Sai mat khau: " + username);
            return null;
        }
    }

    // ── Đăng ký ───────────────────────────────────────────────────────────────


    public boolean register(RegisterRequest request) {
        if (userDAO.getUserByUsername(request.getUsername()) != null) {
            return false;
        }

        // Validate role null-safe
        String role = (request.getRole() != null) ? request.getRole().toUpperCase() : "BIDDER";

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));

        User newUser;
        if ("SELLER".equalsIgnoreCase(role)) {
            newUser = new Seller();
        } else {
            newUser = new Bidder();
        }

        newUser.setUsername(request.getUsername());
        newUser.setPassword(hashedPassword);
        newUser.setRole(role);

        return userDAO.addUser(newUser);
    }

    // ── Lấy profile ───────────────────────────────────────────────────────────

    public User getUserProfile(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            System.err.println("[USER] Khong tim thay user ID: " + userId);
        }
        return user;
    }

    /**
     * Lấy user theo username (dùng trong ClientHandler sau register để lấy id).
     * Tránh gọi login() 2 lần chỉ để lấy id như trước.
     */
    public User getUserByUsername(String username) {
        return userDAO.getUserByUsername(username);
    }

    // ── Nạp tiền ──────────────────────────────────────────────────────────────


    public boolean topUpBalance(int userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("[TOPUP] So tien nap phai lon hon 0!");
            return false;
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            System.err.println("[TOPUP] Khong tim thay user ID: " + userId);
            return false;
        }

        if (!(user instanceof Bidder)) {
            System.err.println("[TOPUP] Chi Bidder moi duoc nap tien. Role hien tai: " + user.getRole());
            return false;
        }

        Bidder bidder = (Bidder) user;

        // Null-safe: nếu balance trong DB là NULL thì coi như 0
        BigDecimal currentBalance = bidder.getBalance() != null ? bidder.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance     = currentBalance.add(amount);
        bidder.setBalance(newBalance);

        return userDAO.updateUser(bidder);
    }

    // ── Đổi mật khẩu ──────────────────────────────────────────────────────────


    public boolean changePassword(String username, String oldPass, String newPass) {
        if (newPass == null || newPass.trim().isEmpty()) {
            System.err.println("[AUTH] Mat khau moi khong duoc de trong!");
            return false;
        }

        User user = userDAO.getUserByUsername(username);
        if (user == null) return false;

        // Kiểm tra mật khẩu cũ bằng BCrypt
        if (!BCrypt.checkpw(oldPass, user.getPassword())) {
            System.err.println("[AUTH] Mat khau cu khong chinh xac!");
            return false;
        }

        // Hash mật khẩu mới trước khi lưu
        String hashedNewPass = BCrypt.hashpw(newPass, BCrypt.gensalt(12));
        user.setPassword(hashedNewPass);
        return userDAO.updateUser(user);
    }

    // ── Cập nhật rating Seller ────────────────────────────────────────────────

    public boolean updateSellerRating(int sellerId, double ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            System.err.println("[RATING] Diem phai tu 1 den 5!");
            return false;
        }

        User user = userDAO.getUserById(sellerId);
        if (!(user instanceof Seller)) {
            System.err.println("[RATING] User ID " + sellerId + " khong phai Seller!");
            return false;
        }

        Seller seller = (Seller) user;
        seller.setRating(ratingValue);
        return userDAO.updateUser(seller);
    }
}