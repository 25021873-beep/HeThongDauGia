package org.example.service;

import org.example.dao.user.UserDAO;
import org.example.dto.RegisterRequest;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.mindrot.jbcrypt.BCrypt;

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
    public boolean register(RegisterRequest request) {
        if (userDAO.getUserByUsername(request.getUsername()) != null) {
            return false;
        }

        String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt(12));

        User newUser;
        if ("SELLER".equalsIgnoreCase(request.getRole())) {
            newUser = new Seller(); // Khởi tạo con của User
        } else {
            newUser = new Bidder(); // Mặc định là Bidder
        }

        newUser.setUsername(request.getUsername());
        newUser.setPassword(hashedPassword);
        newUser.setRole(request.getRole().toUpperCase()); // Lưu role để tí nữa ném xuống DB

        // 5. Lưu xuống Database
        return userDAO.addUser(newUser);
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
    public boolean topUpBalance(int userId, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Từ chối: Tiền nạp phải lớn hơn 0!");
            return false;
        }

        User user = userDAO.getUserById(userId);
        if (user == null) {
            System.err.println("Không tìm thấy User!");
            return false;
        }

        if (user instanceof Bidder) {
            Bidder bidder = (Bidder) user;
            BigDecimal newBalance = bidder.getBalance().add(amount);
            bidder.setBalance(newBalance);

            return userDAO.updateUser(bidder);
        } else {
            System.err.println("Từ chối: " + user.getRole() + " không được phép nạp tiền đấu giá!");
            return false;
        }
    }

    // Hàm đổi mật khẩu
    public boolean changePassword(String username, String oldPass, String newPass) {
        User user = userDAO.getUserByUsername(username);

        if (user != null && user.getPassword().equals(oldPass)) {
            user.setPassword(newPass);
            return userDAO.updateUser(user);
        }

        System.err.println("Mật khẩu cũ không đúng, không cho đổi!");
        return false;
    }

    // Hàm thay đổi Rating Seller
    public boolean updateSellerRating(int sellerId, double ratingValue) {
        if (ratingValue < 1 || ratingValue > 5) {
            System.err.println("Điểm rate rác quá, phải từ 1 đến 5 chứ!");
            return false;
        }

        User user = userDAO.getUserById(sellerId);

        if (user instanceof Seller) {
            Seller seller = (Seller) user;


            seller.setRating(ratingValue);

            return userDAO.updateUser(seller);
        } else {
            System.err.println("Thằng ID " + sellerId + " đéo phải Seller, rate cái đéo gì!");
            return false;
        }
    }
}