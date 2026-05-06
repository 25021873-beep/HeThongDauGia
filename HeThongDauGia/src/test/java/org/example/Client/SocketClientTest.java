package org.example.Client;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class SocketClientTest {

    private SocketClient client;

    @BeforeEach
    void setUp() {
        client = new SocketClient();
    }

    // Test 1: Tạo SocketClient thành công
    @Test
    void testSocketClientCreated() {
        assertNotNull(client);
    }

    // Test 2: Chưa connect thì không gửi được
    @Test
    void testSendBeforeConnect() {
        // Không crash khi gửi mà chưa kết nối
        assertDoesNotThrow(() -> client.login("alice", "123"));
    }

    // Test 3: Đăng ký callback thành công
    @Test
    void testSetOnMessageReceived() {
        assertDoesNotThrow(() ->
                client.setOnMessageReceived(msg ->
                        System.out.println("Nhận: " + msg)
                )
        );
    }

    // Test 4: Disconnect khi chưa connect không crash
    @Test
    void testDisconnectBeforeConnect() {
        assertDoesNotThrow(() -> client.disconnect());
    }

    // Test 5: Kết nối thật đến server
    // Đợi Backend xong server

    @Test
    @Disabled("Chỉ chạy khi Server đang bật")
    void testConnect() {
        assertDoesNotThrow(() -> {
            client.connect();
            client.disconnect();
        });
    }
}