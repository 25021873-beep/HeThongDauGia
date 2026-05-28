package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketConsoleClient {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8888;

    public static void main(String[] args) throws IOException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(HOST, PORT), 3000);

            BufferedReader serverIn = new BufferedReader(
                    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            PrintWriter serverOut = new PrintWriter(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            BufferedReader keyboard = new BufferedReader(
                    new InputStreamReader(System.in, StandardCharsets.UTF_8));

            Thread listener = new Thread(() -> listen(serverIn));
            listener.setDaemon(true);
            listener.start();

            printHelp();
            String line;
            while ((line = keyboard.readLine()) != null) {
                String json = toJsonCommand(line.trim());
                if (json == null) {
                    continue;
                }
                serverOut.println(json);
                if ("logout".equalsIgnoreCase(line.trim()) || "quit".equalsIgnoreCase(line.trim())) {
                    break;
                }
            }
        }
    }

    private static void listen(BufferedReader serverIn) {
        try {
            String line;
            while ((line = serverIn.readLine()) != null) {
                System.out.println("[SERVER] " + line);
            }
        } catch (IOException e) {
            System.out.println("[CLIENT] Mat ket noi server: " + e.getMessage());
        }
    }

    private static String toJsonCommand(String input) {
        if (input.isEmpty()) {
            return null;
        }

        String[] parts = input.split("\\s+");
        String command = parts[0].toLowerCase();

        switch (command) {
            case "register":
                if (parts.length < 5) {
                    System.out.println("Dung: register <username> <password> <email> <BIDDER|SELLER>");
                    return null;
                }
                return "{\"command\":\"REGISTER\",\"username\":\"" + escape(parts[1])
                        + "\",\"password\":\"" + escape(parts[2])
                        + "\",\"email\":\"" + escape(parts[3])
                        + "\",\"role\":\"" + escape(parts[4].toUpperCase()) + "\"}";
            case "login":
                if (parts.length < 3) {
                    System.out.println("Dung: login <username> <password>");
                    return null;
                }
                return "{\"command\":\"LOGIN\",\"username\":\"" + escape(parts[1])
                        + "\",\"password\":\"" + escape(parts[2]) + "\"}";
            case "auctions":
                return "{\"command\":\"GET_ALL_AUCTIONS\"}";
            case "join":
                if (parts.length < 2) {
                    System.out.println("Dung: join <auctionId>");
                    return null;
                }
                return "{\"command\":\"JOIN\",\"auctionId\":" + parts[1] + "}";
            case "bid":
                if (parts.length < 3) {
                    System.out.println("Dung: bid <auctionId> <amount>");
                    return null;
                }
                return "{\"command\":\"BID\",\"auctionId\":" + parts[1]
                        + ",\"amount\":" + parts[2] + "}";
            case "logout":
            case "quit":
                return "{\"command\":\"LOGOUT\"}";
            case "raw":
                return input.substring(3).trim();
            case "help":
                printHelp();
                return null;
            default:
                System.out.println("Lenh khong ho tro. Go help de xem danh sach lenh.");
                return null;
        }
    }

    private static void printHelp() {
        System.out.println("Lenh:");
        System.out.println("  register <username> <password> <email> <BIDDER|SELLER>");
        System.out.println("  login <username> <password>");
        System.out.println("  auctions");
        System.out.println("  join <auctionId>");
        System.out.println("  bid <auctionId> <amount>");
        System.out.println("  raw <json>");
        System.out.println("  logout");
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
