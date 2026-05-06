package org.example.service;

import org.example.dao.ItemDAO;
import org.example.entity.item.Item;
import org.example.entity.user.User;

import java.math.BigDecimal;
import java.util.List;

public class ItemService {
    private final ItemDAO itemDAO = new ItemDAO();

    // Hàm lấy tất cả Item
    public List<Item> getAllItems() {
        System.out.println("Đang tải danh sách mặt hàng từ Database...");
        return itemDAO.getAllItems();
    }

    // Hàm thêm Item mới
    public boolean addNewItem(String itemName, String description, BigDecimal startingPrice, User currentUser) {
        if (currentUser == null) {
            System.out.println("Từ chối: Chưa đăng nhập");
            return false;
        }

        if (currentUser.getRole().equals("BIDDER")) {
            System.out.println("Bidder thêm Item được");
            return false;
        }

        if (itemName == null || itemName.trim().isEmpty()) {
            System.out.println("Từ chối: Tên món hàng không được để trống!");
            return false;
        }

        if (startingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Từ chối: Đăng bán mà giá khởi điểm <= 0 thì bán làm mẹ gì!");
            return false;
        }

        Item newItem = new Item();
        newItem.setName(itemName);
        newItem.setDescription(description);
        newItem.setStartingPrice(startingPrice);

        boolean isSuccess = itemDAO.addItem(newItem);

        if (isSuccess) {
            System.out.println("Ngon: Đại gia " + currentUser.getUsername() + " vừa nhập kho món " + itemName);
        } else {
            System.out.println("Có biến ở tầng Database rồi!");
        }

        return isSuccess;
    }

    // Hàm hiện các Items đang active
    public List<Item> getActiveItems() {
        System.out.println("Đang tải danh sách đồ cổ đang chờ lên thớt...");
        // Giả sử database m lưu trạng thái là 'OPEN' hoặc 'AVAILABLE'
        return itemDAO.getItemsByStatus("AVAILABLE");
    }

    // Hàm hiện Item chi tiết theo Id
    public Item getItemById(int itemId) {
        Item item = itemDAO.getItemById(itemId);
        if (item == null) {
            System.out.println("Lỗi: Mặt hàng ID " + itemId + " không tồn tại hoặc đã bị xóa!");
        }
        return item;
    }

    // Hàm hiện Items theo tên
    public List<Item> searchItemsByName(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveItems();
        }

        System.out.println("Đang tìm kiếm mặt hàng chứa từ khóa: " + keyword);
        String sqlKeyword = "%" + keyword.trim() + "%";
        return itemDAO.searchItems(sqlKeyword);
    }
}