package org.example.service;

import org.example.dao.user.UserDAO;
import org.example.dto.request.RegisterRequest;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.exception.AuctionSystemException;
import org.example.exception.auth.*;
import org.example.exception.balance.InvalidTopUpAmountException;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    private static UserService instance;

    private UserService() {
        // Khóa constructor không cho gọi từ bên ngoài
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    // ── Đăng nhập ─────────────────────────────────────────────────────────────


    public User login(String username, String password) {
        User existingUser = userDAO.getUserByUsername(username);
        if (existingUser == null) throw new InvalidCredentialsException("Lỗi: Sai tài khoản hoặc mật khẩu");

        String dbPassword = existingUser.getPassword();
        boolean passwordMatches = false;
        
        if (dbPassword != null && dbPassword.startsWith("$2")) {
            try {
                passwordMatches = BCrypt.checkpw(password, dbPassword);
            } catch (Exception e) {
                passwordMatches = false;
            }
        } else {
            // Hỗ trợ cho các tài khoản mock data cũ chưa được hash password
            passwordMatches = password.equals(dbPassword);
        }

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Lỗi: Sai tài khoản hoặc mật khẩu");
        }
        return existingUser;
    }

    // ── Đăng ký ───────────────────────────────────────────────────────────────


    public boolean register(RegisterRequest request) {
        if (userDAO.getUserByUsername(request.getUsername()) != null) {
            throw new DuplicateUsernameException("Lỗi: Tên tài khoản đã có người sử dụng");
        }

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

        return (userDAO.addUser(newUser)>=0);
    }

    // ── Lấy profile ───────────────────────────────────────────────────────────

    public User getUserProfile(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("Lỗi: Không tìm thấy người dùng");
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
            throw new InvalidTopUpAmountException("Lỗi: Số tiền nạp không hợp lệ");
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("Lỗi: Không tìm thấy người dùng hợp lệ");
        }

        if (!(user instanceof Bidder)) {
            throw new InvalidRoleException("Lỗi: Không phải bidder thì không được nạp tiền");
        }

        Bidder bidder = (Bidder) user;

        BigDecimal currentBalance = bidder.getBalance() != null ? bidder.getBalance() : BigDecimal.ZERO;
        BigDecimal newBalance     = currentBalance.add(amount);
        bidder.setBalance(newBalance);

        return userDAO.updateUser(bidder);
    }

    // ── Đổi mật khẩu ──────────────────────────────────────────────────────────


    public boolean changePassword(String username, String oldPass, String newPass) {
        if (newPass == null || newPass.trim().isEmpty()) {
            throw new AuctionSystemException("Lỗi: Mật khẩu mới không được để trống");
        }

        User user = userDAO.getUserByUsername(username);
        if (user == null) throw new UserNotFoundException("Lỗi: Không tìm thấy người dùng");

        // Kiểm tra mật khẩu cũ bằng BCrypt
        if (!BCrypt.checkpw(oldPass, user.getPassword())) {
            throw new InvalidCredentialsException("Lỗi: Mật khẩu cũ không chính xác");
        }

        // Hash mật khẩu mới trước khi lưu
        String hashedNewPass = BCrypt.hashpw(newPass, BCrypt.gensalt(12));
        user.setPassword(hashedNewPass);
        return userDAO.updateUser(user);
    }

    // ── Cập nhật rating Seller ────────────────────────────────────────────────

    public boolean updateSellerRating(int sellerId, double ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            throw new SellersRatingException("Lỗi: Rating phải từ 1 đến 5");
        }

        User user = userDAO.getUserById(sellerId);
        if (!(user instanceof Seller)) {
            throw new InvalidRoleException("Lỗi: Không phải seller");
        }

        Seller seller = (Seller) user;
        seller.setRating(ratingValue);
        return userDAO.updateUser(seller);
    }
}