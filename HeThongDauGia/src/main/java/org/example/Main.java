import org.example.dao.UserDAO;
import org.example.entity.User;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        UserDAO userDAO = new UserDAO();
        String testUser = "minh_deptrai_01";
        String testPass = "123456";

        System.out.println("--- BẮT ĐẦU TEST USER_DAO ---");

        // 1. Test hàm checkUsernameExists & addUser
        if (!userDAO.checkUsernameExists(testUser)) {
            User newUser = new User();
            newUser.setUsername(testUser);
            newUser.setPassword(testPass);
            newUser.setRole("BIDDER");
            System.out.println("1. Đăng ký user mới: " + userDAO.addUser(newUser));
        } else {
            System.out.println("1. User đã tồn tại, bỏ qua bước đăng ký.");
        }

        // 2. Test hàm checkLogin
        User loggedIn = userDAO.checkLogin(testUser, testPass);
        System.out.println("2. Đăng nhập với pass cũ: " + (loggedIn != null ? "Thành công!" : "Thất bại!"));

        // 3. Test hàm changePassword
        String newPass = "mat_khau_moi_nhe";
        System.out.println("3. Đổi mật khẩu: " + userDAO.changePassword(newPass, testUser));

        // 4. Test đăng nhập lại bằng mật khẩu mới
        User loggedInNew = userDAO.checkLogin(testUser, newPass);
        System.out.println("4. Đăng nhập với pass mới: " + (loggedInNew != null ? "Thành công!" : "Thất bại!"));

        // 5. Test hàm getAllUsers
        List<User> list = userDAO.getAllUsers();
        System.out.println("5. Tổng số user trong DB hiện tại: " + list.size());
        for (User u : list) {
            System.out.println("   -> ID: " + u.getId() + " | Tên: " + u.getUsername() + " | Vai trò: " + u.getRole());
        }

        System.out.println("--- KẾT THÚC TEST ---");
    }
}