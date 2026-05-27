package com.auction.client.network;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public final class ServerClient {
    private static final String HOST = "26.139.15.134";
    private static final int PORT = 8888;
    private static final int CONNECT_TIMEOUT_MS = 3000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final Gson GSON = new Gson();

    private ServerClient() {
    }

    public static JsonObject sendRequest(JsonObject request) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);

            try (BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                 PrintWriter out = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8)) {

                String welcome = in.readLine();
                System.out.println("[CLIENT] Server welcome: " + welcome);

                String requestJson = GSON.toJson(request);
                System.out.println("[CLIENT] Gui request: " + hidePassword(requestJson));
                out.println(requestJson);

                String response = in.readLine();
                if (response == null || response.trim().isEmpty()) {
                    throw new IOException("Server khong tra ve phan hoi");
                }
                System.out.println("[CLIENT] Nhan response: " + response);
                return GSON.fromJson(response, JsonObject.class);
            }
        }
    }

    public static boolean isSuccess(JsonObject response) {
        return response != null
                && response.has("status")
                && "SUCCESS".equalsIgnoreCase(response.get("status").getAsString());
    }

    public static String messageOf(JsonObject response) {
        if (response != null && response.has("message")) {
            return response.get("message").getAsString();
        }
        return "Khong co phan hoi tu server";
    }

    private static String hidePassword(String json) {
        return json.replaceAll("\"password\"\\s*:\\s*\"[^\"]*\"", "\"password\":\"***\"");
    }
}
