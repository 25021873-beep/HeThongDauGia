package org.example;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MainTest {

    @Test
    void socketConsoleClientConvertsSupportedCommandsToJsonProtocol() throws Exception {
        assertEquals(
                "{\"command\":\"LOGIN\",\"username\":\"alice\",\"password\":\"secret\"}",
                toJsonCommand("login alice secret"));
        assertEquals(
                "{\"command\":\"BID\",\"auctionId\":7,\"amount\":1500}",
                toJsonCommand("bid 7 1500"));
        assertEquals(
                "{\"command\":\"LOGOUT\"}",
                toJsonCommand("logout"));
    }

    @Test
    void socketConsoleClientIgnoresBlankInput() throws Exception {
        assertNull(toJsonCommand(""));
    }

    private static String toJsonCommand(String input) throws Exception {
        Method method = SocketConsoleClient.class.getDeclaredMethod("toJsonCommand", String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, input);
    }
}
