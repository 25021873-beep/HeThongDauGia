package org.example.service;

import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.exception.item.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ItemService {
    private ItemDAO itemDAO = new ItemDAO();
    private UserDAO userDAO = new UserDAO();

    private static ItemService instance;

    private ItemService() {
    }

    public static synchronized ItemService getInstance() {
        if (instance == null) {
            instance = new ItemService();
        }
        return instance;
    }

    // Hàm đăng bán
    public int postItem(Item newItem) {
        if (newItem.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidItemPriceException("Lỗi: Giá Item không được bé hơn 0 ");
        }
        if (newItem.getName() == null || newItem.getName().trim().isEmpty()) {
            throw new InvalidItemNameException("Lỗi: Không đựợc để trống tên sản phẩm");
        }

        User user = userDAO.getUserById(newItem.getSellerId());
        if (!(user instanceof Seller)) {
            throw new UnauthorizedAccessException("Lỗi: Không thể đăng bán nếu bạn không phải Seller");
        }

        newItem.setStatus("AVAILABLE");

        return itemDAO.addItem(newItem);
    }

    // Hàm chỉnh sửa thông tin
    public boolean updateItem(Item updatedItem, int requesterId) {
        Item existingItem = itemDAO.getItemById(updatedItem.getId());

        if (existingItem == null) {
            throw new ItemNotFoundException("Lỗi: Không tìm thấy sản phẩm");
        }

        User requester = userDAO.getUserById(requesterId);
        boolean isAdmin = requester != null && requester.getRole().equals("ADMIN");

        if (!isAdmin && existingItem.getSellerId() != requesterId) {
            throw new UnauthorizedAccessException("Lỗi: Không phải chủ sản phẩm thì không thể sửa thông tin");
        }

        if (!existingItem.getStatus().equals("AVAILABLE")) {
            throw new InvalidItemStateException("Lỗi: Sản phẩm đã được bán, không thể sửa thông tin");
        }

        updatedItem.setSellerId(existingItem.getSellerId());
        return itemDAO.updateItem(updatedItem);
    }

    // Hàm đổi trạng thái (AVAILABLE -> IN_AUCTION -> SOLD/UNSOLD)
    public boolean changeItemStatus(int itemId, String newStatus) {
        Item existingItem = itemDAO.getItemById(itemId);
        if (existingItem == null) {
            throw new ItemNotFoundException("Lỗi: Không tìm thấy sản phẩm");
        }

        String currentStatus = existingItem.getStatus();

        if (currentStatus.equals("SOLD") && newStatus.equals("IN_AUCTION")) {
            throw new InvalidItemStateException("Lỗi: Sản phẩm đã được bán hoặc đang đấu giá, không thể sửa trạng thái");
        }

        return itemDAO.updateItemStatus(itemId, newStatus);
    }

    // 4. Xóa/Rút món hàng
    public boolean deleteItem(int itemId, int requesterId) {
        Item existingItem = itemDAO.getItemById(itemId);
        if (existingItem == null) {
            throw new ItemNotFoundException("Lỗi: Không tìm thấy sản phẩm");
        }

        User requester = userDAO.getUserById(requesterId);
        boolean isAdmin = requester != null && requester.getRole().equals("ADMIN");

        // Admin xóa thằng nào cũng được, user thường thì chỉ được xóa đồ của mình
        if (!isAdmin && existingItem.getSellerId() != requesterId) {
            throw new UnauthorizedAccessException("Lỗi: Không phải chủ sản phẩm/admin thì không thể xóa sản phẩm");
        }

        if (!existingItem.getStatus().equals("AVAILABLE") && !isAdmin) {
            throw new InvalidItemStateException("Lỗi: Sản phẩm đang được đấu giá, không thể xóa");
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




