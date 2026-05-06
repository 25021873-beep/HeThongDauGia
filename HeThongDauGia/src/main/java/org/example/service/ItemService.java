package org.example.service;

import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.entity.user.Seller;
import org.example.entity.user.User;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemService {
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO();

    // Hàm đăng bán
    public boolean postItem(Item newItem) {
        if (newItem.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            System.err.println("Từ chối: Giá khởi điểm phải lớn hơn 0!");
            return false;
        }
        if (newItem.getName() == null || newItem.getName().trim().isEmpty()) {
            System.err.println("Từ chối: Tên sản phẩm không được để trống!");
            return false;
        }

        User user = userDAO.getUserById(newItem.getSellerId());
        if (!(user instanceof Seller)) {
            System.err.println("Từ chối: Mày không phải Seller, lấy tư cách gì đăng bán?");
            return false;
        }

        newItem.setStatus("AVAILABLE");

        return itemDAO.addItem(newItem);
    }

    // Hàm chỉnh sửa thông tin
    public boolean updateItem(Item updatedItem, int requesterId) {
        Item existingItem = itemDAO.getItemById(updatedItem.getId());

        if (existingItem == null) {
            System.err.println("Lỗi: Không tìm thấy món hàng này trong kho!");
            return false;
        }

        User requester = userDAO.getUserById(requesterId);
        boolean isAdmin = requester != null && requester.getRole().equals("ADMIN");

        if (!isAdmin && existingItem.getSellerId() != requesterId) {
            System.err.println("Từ chối: Mày không phải chủ món hàng, cấm sửa!");
            return false;
        }

        if (!existingItem.getStatus().equals("AVAILABLE")) {
            System.err.println("Từ chối: Hàng đang đấu giá hoặc đã bán, không được phép sửa!");
            return false;
        }

        updatedItem.setSellerId(existingItem.getSellerId());
        return itemDAO.updateItem(updatedItem);
    }

    // Hàm đổi trạng thái (AVAILABLE -> IN_AUCTION -> SOLD/UNSOLD)
    public boolean changeItemStatus(int itemId, String newStatus) {
        Item existingItem = itemDAO.getItemById(itemId);
        if (existingItem == null) return false;

        String currentStatus = existingItem.getStatus();

        if (currentStatus.equals("SOLD") && newStatus.equals("IN_AUCTION")) {
            System.err.println("Lỗi Logic: Hàng đã bán sao quay lại đấu giá được!");
            return false;
        }

        return itemDAO.updateItemStatus(itemId, newStatus);
    }

    // 4. Xóa/Rút món hàng
    public boolean deleteItem(int itemId, int requesterId) {
        Item existingItem = itemDAO.getItemById(itemId);
        if (existingItem == null) return false;

        User requester = userDAO.getUserById(requesterId);
        boolean isAdmin = requester != null && requester.getRole().equals("ADMIN");

        // Admin xóa thằng nào cũng được, user thường thì chỉ được xóa đồ của mình
        if (!isAdmin && existingItem.getSellerId() != requesterId) {
            System.err.println("Không được xóa đồ của người khác");
            return false;
        }

        if (!existingItem.getStatus().equals("AVAILABLE") && !isAdmin) {
            System.err.println("Từ chối: Đồ đang có người giành nhau, m không được phép rút!");
            return false;
        }

        return itemDAO.deleteItem(itemId);
    }

    // 5. Hiển thị đồ cho trang chủ Bidder
    public List<Item> getHomepageItems(String keyword, String category) {
        List<Item> rawItems;

        if (keyword != null && !keyword.trim().isEmpty()) {
            rawItems = itemDAO.searchItems(keyword);
        } else {
            rawItems = itemDAO.getAllItems();
        }

        List<Item> filteredItems = new ArrayList<>();

        for (Item item : rawItems) {

            if (item.getStatus().equals("SOLD")) {
                continue;
            }

            if (category == null || category.trim().isEmpty()) {
                filteredItems.add(item);
            } else {
                if (category.equalsIgnoreCase("ELECTRONICS") && item instanceof Electronics) {
                    filteredItems.add(item);
                }
                else if (category.equalsIgnoreCase("ART") && item instanceof Art) {
                    filteredItems.add(item);
                }
                else if (category.equalsIgnoreCase("VEHICLE") && item instanceof Vehicle) {
                    filteredItems.add(item);
                }
            }
        }

        return filteredItems;
    }
}




