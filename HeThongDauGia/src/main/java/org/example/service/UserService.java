package org.example.service;

import org.example.dao.UserDAO;
import org.example.entity.User;

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
}