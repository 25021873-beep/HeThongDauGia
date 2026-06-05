package org.example.service;

import org.example.dao.item.ItemDAO;
import org.example.dao.user.UserDAO;
import org.example.entity.item.Art;
import org.example.entity.item.Electronics;
import org.example.entity.item.Item;
import org.example.entity.item.Vehicle;
import org.example.entity.user.Admin;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.exception.item.InvalidItemNameException;
import org.example.exception.item.InvalidItemStateException;
import org.example.exception.item.UnauthorizedAccessException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemServiceTest {

    private final ItemService service = ItemService.getInstance();

    @AfterEach
    void tearDown() {
        service.resetForTesting();
    }

    @Test
    void postItemHappyCaseRequiresSellerAndMarksItemAvailable() {
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        RecordingUserDAO userDAO = new RecordingUserDAO(seller(7));
        service.setDaosForTesting(itemDAO, userDAO);
        Electronics item = validElectronics(0, 7, "Laptop", "D", "DRAFT");

        int newItemId = service.postItem(item);

        assertAll(
                () -> assertEquals(55, newItemId),
                () -> assertSame(item, itemDAO.addedItem),
                () -> assertEquals("AVAILABLE", item.getStatus())
        );
    }

    @Test
    void postItemRejectsBlankNameBeforeDatabaseLookup() {
        service.setDaosForTesting(new RecordingItemDAO(), new RecordingUserDAO());
        Electronics item = validElectronics(0, 1, " ", "D", null);

        assertThrows(InvalidItemNameException.class, () -> service.postItem(item));
    }

    @Test
    void postItemRejectsNonSellerUser() {
        service.setDaosForTesting(new RecordingItemDAO(), new RecordingUserDAO(bidder(9)));
        Electronics item = validElectronics(0, 9, "Laptop", "D", null);

        assertThrows(UnauthorizedAccessException.class, () -> service.postItem(item));
    }

    @Test
    void updateItemHappyCaseAllowsOwnerAndPreservesOriginalSeller() {
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        RecordingUserDAO userDAO = new RecordingUserDAO(seller(7));
        Electronics existing = validElectronics(10, 7, "Old", "Old desc", "AVAILABLE");
        Electronics updated = validElectronics(10, 99, "New", "New desc", "AVAILABLE");
        itemDAO.put(existing);
        service.setDaosForTesting(itemDAO, userDAO);

        assertTrue(service.updateItem(updated, 7));

        assertAll(
                () -> assertSame(updated, itemDAO.updatedItem),
                () -> assertEquals(7, updated.getSellerId())
        );
    }

    @Test
    void updateItemRejectsNonOwnerAndNonAdmin() {
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        itemDAO.put(validElectronics(10, 7, "Laptop", "D", "AVAILABLE"));
        service.setDaosForTesting(itemDAO, new RecordingUserDAO(seller(8)));

        assertThrows(UnauthorizedAccessException.class,
                () -> service.updateItem(validElectronics(10, 8, "New", "D", "AVAILABLE"), 8));
    }

    @Test
    void deleteItemAllowsAdminEvenWhenItemIsInAuction() {
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        itemDAO.put(validElectronics(10, 7, "Laptop", "D", "IN_AUCTION"));
        service.setDaosForTesting(itemDAO, new RecordingUserDAO(admin(1)));

        assertTrue(service.deleteItem(10, 1));

        assertEquals(10, itemDAO.deletedItemId);
    }

    @Test
    void deleteItemRejectsNonOwnerAndRejectsOwnerWhenItemIsInAuction() {
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        itemDAO.put(validElectronics(10, 7, "Laptop", "D", "AVAILABLE"));
        service.setDaosForTesting(itemDAO, new RecordingUserDAO(seller(8)));

        assertThrows(UnauthorizedAccessException.class, () -> service.deleteItem(10, 8));

        itemDAO.put(validElectronics(11, 7, "Phone", "D", "IN_AUCTION"));
        service.setDaosForTesting(itemDAO, new RecordingUserDAO(seller(7)));
        assertThrows(InvalidItemStateException.class, () -> service.deleteItem(11, 7));
    }

    @Test
    void homepageItemsFilterOutSoldItemsAndMatchCategory() {
        RecordingItemDAO itemDAO = new RecordingItemDAO();
        itemDAO.allItems.add(validElectronics(1, 7, "Laptop", "D", "AVAILABLE"));
        itemDAO.allItems.add(art(2, 7, "Painting", "AVAILABLE"));
        itemDAO.allItems.add(vehicle(3, 7, "Car", "SOLD"));
        service.setDaosForTesting(itemDAO, new RecordingUserDAO());

        List<Item> electronics = service.getHomepageItems(null, "ELECTRONICS");
        List<Item> allVisible = service.getHomepageItems(null, null);

        assertAll(
                () -> assertEquals(1, electronics.size()),
                () -> assertInstanceOf(Electronics.class, electronics.get(0)),
                () -> assertEquals(2, allVisible.size()),
                () -> assertTrue(allVisible.stream().noneMatch(item -> "SOLD".equals(item.getStatus())))
        );
    }

    private static Electronics validElectronics(int id, int sellerId, String name, String description, String status) {
        Electronics item = new Electronics();
        item.setId(id);
        item.setSellerId(sellerId);
        item.setName(name);
        item.setDescription(description);
        item.setStatus(status);
        item.setWarrantyMonths(12);
        return item;
    }

    private static Art art(int id, int sellerId, String name, String status) {
        Art item = new Art();
        item.setId(id);
        item.setSellerId(sellerId);
        item.setName(name);
        item.setStatus(status);
        item.setAuthor("Author");
        return item;
    }

    private static Vehicle vehicle(int id, int sellerId, String name, String status) {
        Vehicle item = new Vehicle();
        item.setId(id);
        item.setSellerId(sellerId);
        item.setName(name);
        item.setStatus(status);
        item.setEngineType("V8");
        return item;
    }

    private static Seller seller(int id) {
        Seller seller = new Seller();
        seller.setId(id);
        seller.setUsername("seller" + id);
        return seller;
    }

    private static Bidder bidder(int id) {
        Bidder bidder = new Bidder();
        bidder.setId(id);
        bidder.setUsername("bidder" + id);
        return bidder;
    }

    private static Admin admin(int id) {
        Admin admin = new Admin();
        admin.setId(id);
        admin.setUsername("admin" + id);
        return admin;
    }

    private static final class RecordingItemDAO extends ItemDAO {
        private final Map<Integer, Item> items = new HashMap<>();
        private final List<Item> allItems = new ArrayList<>();
        private Item addedItem;
        private Item updatedItem;
        private int deletedItemId = -1;

        private void put(Item item) {
            items.put(item.getId(), item);
        }

        @Override
        public int addItem(Item item) {
            this.addedItem = item;
            item.setId(55);
            put(item);
            return 55;
        }

        @Override
        public Item getItemById(int id) {
            return items.get(id);
        }

        @Override
        public boolean updateItem(Item item) {
            this.updatedItem = item;
            put(item);
            return true;
        }

        @Override
        public boolean deleteItem(int id) {
            this.deletedItemId = id;
            return items.remove(id) != null;
        }

        @Override
        public List<Item> getAllItems() {
            return allItems;
        }

        @Override
        public List<Item> searchItems(String keyword) {
            return allItems.stream()
                    .filter(item -> item.getName() != null && item.getName().contains(keyword))
                    .toList();
        }
    }

    private static final class RecordingUserDAO extends UserDAO {
        private final Map<Integer, User> users = new HashMap<>();

        private RecordingUserDAO(User... users) {
            for (User user : users) {
                this.users.put(user.getId(), user);
            }
        }

        @Override
        public User getUserById(int id) {
            return users.get(id);
        }
    }
}
