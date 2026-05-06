package org.example.service;

import org.example.dao.UserDAO;
import org.example.entity.user.User;

import java.math.BigDecimal;

public class UserService {
    private final UserDAO userDAO = new UserDAO();

    // Hàm đăng nhập
    public User login(String username, String password) {
        User existingUser = userDAO.getUserByUsername(username);

        if (existingUser == null) {
            return null;
        }

        if (existingUser.getPassword().equals(password)) {
            System.out.println("Đăng nhập ngon: Chào mừng " + username + " đã vào phòng!");
            return existingUser;
        } else {
            System.out.println("Đăng nhập xịt: Sai mật khẩu!");
            return null;
        }
    }

    // Hàm đăng ký
    public boolean register(String username, String password, String email) {
        User existingUser = userDAO.getUserByUsername(username);
        if (existingUser != null) {
            System.out.println("Đăng ký xịt: Tên tài khoản '" + username + "' đã có thằng xài rồi!");
            return false;
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole("BIDDER");

        boolean isSuccess = userDAO.addUser(newUser);

        if (isSuccess) {
            System.out.println("Đăng ký ngon: Đã tạo tài khoản " + username);
        } else {
            System.out.println("Lỗi Database rồi!");
        }

        return isSuccess;
    }

    // Lấy thông tin profile user
    public User getUserProfile(int userId) {
        User user = userDAO.getUserById(userId);
        if (user == null) {
            System.out.println("Lỗi: Không tìm thấy data của user ID " + userId);
        }
        return user;
    }

    // Hàm nạp tiền vào ví
    public boolean topUpWallet(int userId, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Từ chối nạp: Số tiền nạp vào phải lớn hơn 0!");
            return false;
        }

        boolean isSuccess = userDAO.addBalance(amount, userId);
        if (isSuccess) {
            System.out.println("Nạp thành công " + amount + " vào ví của User ID: " + userId);
        } else {
            System.out.println("Nạp tạch: Lỗi CSDL hoặc không tìm thấy User.");
        }
        return isSuccess;
    }
}