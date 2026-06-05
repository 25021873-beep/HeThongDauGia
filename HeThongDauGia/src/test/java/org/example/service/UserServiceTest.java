package org.example.service;

import org.example.dao.user.UserDAO;
import org.example.dto.request.RegisterRequest;
import org.example.entity.user.Admin;
import org.example.entity.user.Bidder;
import org.example.entity.user.Seller;
import org.example.entity.user.User;
import org.example.exception.AuctionSystemException;
import org.example.exception.auth.AccountLockedException;
import org.example.exception.auth.DuplicateUsernameException;
import org.example.exception.auth.InvalidCredentialsException;
import org.example.exception.auth.InvalidRoleException;
import org.example.exception.auth.SellersRatingException;
import org.example.exception.balance.InvalidTopUpAmountException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private final UserService service = UserService.getInstance();

    @AfterEach
    void tearDown() {
        service.resetForTesting();
    }

    @Test
    void registerBidderHappyCaseHashesPasswordAndSavesBidder() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        RegisterRequest request = registerRequest("alice", "password123", "alice@example.com", "BIDDER");

        assertTrue(service.register(request));

        assertAll(
                () -> assertInstanceOf(Bidder.class, userDAO.addedUser),
                () -> assertEquals("alice", userDAO.addedUser.getUsername()),
                () -> assertEquals("alice@example.com", userDAO.addedUser.getEmail()),
                () -> assertEquals("BIDDER", userDAO.addedUser.getRole()),
                () -> assertNotEquals("password123", userDAO.addedUser.getPassword()),
                () -> assertTrue(BCrypt.checkpw("password123", userDAO.addedUser.getPassword()))
        );
    }

    @Test
    void registerSellerHappyCaseSavesSellerRole() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        RegisterRequest request = registerRequest("seller1", "password123", "seller@example.com", "seller");

        assertTrue(service.register(request));

        assertAll(
                () -> assertInstanceOf(Seller.class, userDAO.addedUser),
                () -> assertEquals("SELLER", userDAO.addedUser.getRole()),
                () -> assertTrue(BCrypt.checkpw("password123", userDAO.addedUser.getPassword()))
        );
    }

    @Test
    void registerRejectsDuplicateUsernameInvalidEmailAndInvalidRole() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        userDAO.put(bidder(1, "alice", "hash", new BigDecimal("1000")));

        assertAll(
                () -> assertThrows(DuplicateUsernameException.class,
                        () -> service.register(registerRequest("alice", "password123", "alice@example.com", "BIDDER"))),
                () -> assertThrows(AuctionSystemException.class,
                        () -> service.register(registerRequest("bob", "password123", "bad-email", "BIDDER"))),
                () -> assertThrows(InvalidRoleException.class,
                        () -> service.register(registerRequest("charlie", "password123", "charlie@example.com", "ADMIN")))
        );
    }

    @Test
    void loginHappyCaseReturnsUserWithValidBcryptPassword() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        Bidder bidder = bidder(2, "bidder2", BCrypt.hashpw("secret", BCrypt.gensalt(4)), new BigDecimal("2000"));
        userDAO.put(bidder);

        User loggedIn = service.login("bidder2", "secret");

        assertSame(bidder, loggedIn);
    }

    @Test
    void loginRejectsWrongPasswordAndLockedAccount() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        Bidder bidder = bidder(2, "bidder2", BCrypt.hashpw("secret", BCrypt.gensalt(4)), new BigDecimal("2000"));
        Bidder locked = bidder(3, "locked", BCrypt.hashpw("secret", BCrypt.gensalt(4)), new BigDecimal("2000"));
        locked.setLocked(true);
        userDAO.put(bidder);
        userDAO.put(locked);

        assertAll(
                () -> assertThrows(InvalidCredentialsException.class,
                        () -> service.login("bidder2", "wrong")),
                () -> assertThrows(AccountLockedException.class,
                        () -> service.login("locked", "secret"))
        );
    }

    @Test
    void topUpBalanceHappyCaseUpdatesBidderBalance() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        Bidder bidder = bidder(2, "bidder2", "hash", new BigDecimal("1000"));
        userDAO.put(bidder);

        assertTrue(service.topUpBalance(2, new BigDecimal("500")));

        assertAll(
                () -> assertSame(bidder, userDAO.updatedUser),
                () -> assertEquals(new BigDecimal("1500"), bidder.getBalance())
        );
    }

    @Test
    void topUpBalanceRejectsNullAndNonPositiveAmountsBeforeDatabaseLookup() {
        install(new RecordingUserDAO());

        assertAll(
                () -> assertThrows(InvalidTopUpAmountException.class, () -> service.topUpBalance(1, null)),
                () -> assertThrows(InvalidTopUpAmountException.class, () -> service.topUpBalance(1, BigDecimal.ZERO)),
                () -> assertThrows(InvalidTopUpAmountException.class, () -> service.topUpBalance(1, new BigDecimal("-1")))
        );
    }

    @Test
    void topUpBalanceRejectsSellerRole() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        Seller seller = seller(4, "seller4");
        userDAO.put(seller);

        assertThrows(InvalidRoleException.class,
                () -> service.topUpBalance(4, new BigDecimal("500")));
    }

    @Test
    void changePasswordHappyCaseStoresNewHash() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        Bidder bidder = bidder(2, "bidder2", BCrypt.hashpw("old-pass", BCrypt.gensalt(4)), new BigDecimal("1000"));
        userDAO.put(bidder);

        assertTrue(service.changePassword("bidder2", "old-pass", "new-pass"));

        assertAll(
                () -> assertSame(bidder, userDAO.updatedUser),
                () -> assertTrue(BCrypt.checkpw("new-pass", bidder.getPassword())),
                () -> assertFalse(BCrypt.checkpw("old-pass", bidder.getPassword()))
        );
    }

    @Test
    void changePasswordRejectsBlankNewPasswordBeforeDatabaseLookup() {
        install(new RecordingUserDAO());

        assertAll(
                () -> assertThrows(AuctionSystemException.class,
                        () -> service.changePassword("alice", "old", null)),
                () -> assertThrows(AuctionSystemException.class,
                        () -> service.changePassword("alice", "old", " "))
        );
    }

    @Test
    void updateSellerRatingRejectsOutOfRangeRatingBeforeDatabaseLookup() {
        install(new RecordingUserDAO());

        assertAll(
                () -> assertThrows(SellersRatingException.class, () -> service.updateSellerRating(1, 0.9)),
                () -> assertThrows(SellersRatingException.class, () -> service.updateSellerRating(1, 5.1))
        );
    }

    @Test
    void toggleUserLockHappyCaseRejectsAdminTarget() {
        RecordingUserDAO userDAO = install(new RecordingUserDAO());
        Bidder bidder = bidder(2, "bidder2", "hash", new BigDecimal("1000"));
        Admin admin = new Admin();
        admin.setId(1);
        admin.setUsername("admin");
        userDAO.put(bidder);
        userDAO.put(admin);

        assertTrue(service.toggleUserLock("bidder2", true));
        assertAll(
                () -> assertEquals("bidder2", userDAO.lockedUsername),
                () -> assertTrue(userDAO.lockStatus),
                () -> assertThrows(AuctionSystemException.class,
                        () -> service.toggleUserLock("admin", true))
        );
    }

    private RecordingUserDAO install(RecordingUserDAO userDAO) {
        service.setUserDAOForTesting(userDAO);
        return userDAO;
    }

    private static RegisterRequest registerRequest(String username, String password, String email, String role) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword(password);
        request.setEmail(email);
        request.setRole(role);
        return request;
    }

    private static Bidder bidder(int id, String username, String password, BigDecimal balance) {
        Bidder bidder = new Bidder();
        bidder.setId(id);
        bidder.setUsername(username);
        bidder.setPassword(password);
        bidder.setEmail(username + "@example.com");
        bidder.setBalance(balance);
        return bidder;
    }

    private static Seller seller(int id, String username) {
        Seller seller = new Seller();
        seller.setId(id);
        seller.setUsername(username);
        seller.setEmail(username + "@example.com");
        return seller;
    }

    private static final class RecordingUserDAO extends UserDAO {
        private final Map<String, User> usersByUsername = new HashMap<>();
        private final Map<Integer, User> usersById = new HashMap<>();
        private User addedUser;
        private User updatedUser;
        private String lockedUsername;
        private boolean lockStatus;

        private void put(User user) {
            usersByUsername.put(user.getUsername(), user);
            usersById.put(user.getId(), user);
        }

        @Override
        public User getUserByUsername(String username) {
            return usersByUsername.get(username);
        }

        @Override
        public User getUserById(int id) {
            return usersById.get(id);
        }

        @Override
        public int addUser(User user) {
            this.addedUser = user;
            user.setId(100);
            put(user);
            return 100;
        }

        @Override
        public boolean updateUser(User user) {
            this.updatedUser = user;
            put(user);
            return true;
        }

        @Override
        public boolean setUserLockStatus(String username, boolean locked) {
            this.lockedUsername = username;
            this.lockStatus = locked;
            User user = usersByUsername.get(username);
            if (user != null) {
                user.setLocked(locked);
            }
            return user != null;
        }
    }
}
