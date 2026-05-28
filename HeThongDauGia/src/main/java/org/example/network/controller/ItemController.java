package org.example.network.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.example.dto.response.SimpleResponse;
import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.network.SessionContext;
import org.example.service.AuctionService;
import org.example.service.ItemService;
import org.example.dao.item.ItemDAO;

import java.time.LocalDateTime;
import java.util.List;

public class ItemController {

    private final SessionContext session;
    private final ItemService    itemService;
    private final Gson           gson;

    public ItemController(SessionContext session, ItemService itemService, Gson gson) {
        this.session     = session;
        this.itemService = itemService;
        this.gson        = gson;
    }

    // ── POST_ITEM (Seller đăng bán sản phẩm mới) ──────────────────────────────

    public void handlePostItem(JsonObject json) {
        if (!session.requireLogin()) return;

        try {
            // Xác định loại sản phẩm dựa trên trường "itemType" gửi từ Client
            if (!json.has("itemType")) {
                session.send(SimpleResponse.error("Thieu thong tin loai san pham (itemType)"));
                return;
            }

            String itemType = json.get("itemType").getAsString().trim().toUpperCase();
            Item newItem;

            // Áp dụng tính đa hình giải mã JSON thành đúng Object thực tế
            switch (itemType) {
                case "ELECTRONICS":
                    newItem = gson.fromJson(json, Electronics.class);
                    break;
                case "ART":
                    newItem = gson.fromJson(json, Art.class);
                    break;
                case "VEHICLE":
                    newItem = gson.fromJson(json, Vehicle.class);
                    break;
                default:
                    session.send(SimpleResponse.error("Loai san pham khong hop le: " + itemType));
                    return;
            }

            // Gắn ID của người dùng hiện tại đang đăng nhập làm SellerId cho món hàng
            int currentUserId = session.getCurrentUser().getId();
            newItem.setSellerId(currentUserId);

            // Gọi Service để xử lý validate logic và lưu xuống DB
            int newItemId = itemService.postItem(newItem);

            if (newItemId > 0) {
                int duration = json.has("duration") ? json.get("duration").getAsInt() : 30;
                AuctionService.getInstance().openAuction(newItemId, LocalDateTime.now(), LocalDateTime.now().plusMinutes(duration));
                session.send(SimpleResponse.success("Dang ban san pham moi va mo phien dau gia thanh cong!"));
            } else {
                session.send(SimpleResponse.error("Khong the dang ban san pham. Vui long kiem tra lai dữ liệu"));
            }

        } catch (Exception e) {
            System.err.println("[ITEM_CTRL] Loi dang ban san pham: " + e.getMessage());
            session.send(SimpleResponse.error("Loi he thong: " + e.getMessage()));
        }
    }

    // ── GET_SELLER_ITEMS (Lấy danh sách sản phẩm của Seller) ──────────────────

    public void handleGetSellerItems() {
        if (!session.requireLogin()) return;

        try {
            int currentUserId = session.getCurrentUser().getId();
            ItemDAO itemDAO = new ItemDAO();
            List<Item> items = itemDAO.getItemsBySeller(currentUserId);
            
            JsonObject response = new JsonObject();
            response.addProperty("status", "SUCCESS");
            
            JsonArray itemsArray = new JsonArray();
            for (Item item : items) {
                JsonObject itemJson = new JsonObject();
                itemJson.addProperty("id", item.getId());
                itemJson.addProperty("name", item.getName());
                itemJson.addProperty("description", item.getDescription());
                itemJson.addProperty("startingPrice", item.getStartingPrice().doubleValue());
                itemJson.addProperty("status", item.getStatus());
                
                if (item instanceof Electronics) itemJson.addProperty("itemType", "Điện tử");
                else if (item instanceof Art) itemJson.addProperty("itemType", "Nghệ thuật");
                else if (item instanceof Vehicle) itemJson.addProperty("itemType", "Xe cộ");
                else itemJson.addProperty("itemType", "Khác");
                
                itemsArray.add(itemJson);
            }
            response.add("items", itemsArray);
            session.send(response);
            
        } catch (Exception e) {
            System.err.println("[ITEM_CTRL] Loi lay danh sach san pham: " + e.getMessage());
            session.send(SimpleResponse.error("Loi: " + e.getMessage()));
        }
    }

    // ── DELETE_ITEM (Seller/Admin xóa sản phẩm) ──────────────────────────────

    public void handleDeleteItem(JsonObject json) {
        if (!session.requireLogin()) return;

        if (!json.has("itemId")) {
            session.send(SimpleResponse.error("Thieu thong tin ID san pham de xoa"));
            return;
        }

        try {
            int itemId = json.get("itemId").getAsInt();
            int requesterId = session.getCurrentUser().getId();

            // Gọi sang ItemService xử lý phân quyền (Chỉ Admin hoặc chính chủ Seller mới được xóa)
            boolean success = itemService.deleteItem(itemId, requesterId);

            if (success) {
                session.send(SimpleResponse.success("Xoa san pham khoi he thong thanh cong!"));
            } else {
                session.send(SimpleResponse.error("Xoa san pham that bai. Vui long thu lai"));
            }

        } catch (Exception e) {
            System.err.println("[ITEM_CTRL] Loi xoa san pham: " + e.getMessage());
            session.send(SimpleResponse.error("Loi: " + e.getMessage()));
        }
    }
}