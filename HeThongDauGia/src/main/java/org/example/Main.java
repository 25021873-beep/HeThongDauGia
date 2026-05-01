import org.example.dao.UserDAO;
import org.example.entity.User;
import org.example.dao.ItemDAO;
import org.example.entity.Item;
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

        ItemDAO itemDAO = new ItemDAO();

        System.out.println("--- BẮT ĐẦU TEST ITEM_DAO ---");

        // 1. Test thêm Item
        Item newItem = new Item();
        newItem.setName("Bình gốm Bát Tràng cổ");
        newItem.setDescription("Hàng limited, không sứt mẻ");
        newItem.setStartingPrice(500000);
        newItem.setSellerId(1);

        boolean isAdded = itemDAO.addItem(newItem);
        System.out.println("1. Thêm sản phẩm thành công? " + isAdded);

        // 2. Test lấy danh sách
        List<Item> items = itemDAO.getAllItems();
        System.out.println("2. Tổng số món hàng trên hệ thống: " + items.size());
        for (Item i : items) {
            System.out.println("   -> [" + i.getId() + "] " + i.getName() + " (Giá KĐ: " + i.getStartingPrice() + ") - Của người bán ID: " + i.getSellerId());
        }

        int testItemId = 1; // Giả sử ID của món hàng m muốn test là 1

        System.out.println("--- BẮT ĐẦU TEST BỔ SUNG ITEM_DAO ---");

        // 1. Test lấy chi tiết Item
        Item foundItem = itemDAO.getItemById(testItemId);
        if (foundItem != null) {
            System.out.println("1. Tìm thấy hàng: " + foundItem.getName());

            // 2. Test sửa Item (Cập nhật giá và mô tả)
            foundItem.setStartingPrice(600000);
            foundItem.setDescription("Đã sửa: Hàng chốt giá cao hơn tí nhé");
            boolean isUpdated = itemDAO.updateItem(foundItem);
            System.out.println("2. Cập nhật thông tin thành công? " + isUpdated);

        } else {
            System.out.println("1. Không tìm thấy Item nào có ID = " + testItemId);
        }

        // 3. Test xóa Item (Cẩn thận chạy xong là bay luôn dòng data trong DB)
        // boolean isDeleted = itemDAO.deleteItem(testItemId);
        // System.out.println("3. Xóa Item thành công? " + isDeleted);
    }
}
