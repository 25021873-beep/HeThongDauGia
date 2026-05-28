package com.auction.client;
import com.google.gson.Gson;

public class Message {
    private String type; //loại
    private String payload; // ruột
    private String senderId; // người gửi
    private long timestamp; // thời gian chính xác tin nhắn được gửi

    // Định nghĩa tất cả các loại tin nhắn ở đây
    public static final String LOGIN         = "LOGIN";
    public static final String LOGIN_OK      = "LOGIN_OK";
    public static final String LOGIN_FAIL    = "LOGIN_FAIL";
    public static final String PLACE_BID     = "PLACE_BID";
    public static final String BID_UPDATE    = "BID_UPDATE";
    public static final String BID_REJECTED  = "BID_REJECTED";
    public static final String JOIN_AUCTION  = "JOIN_AUCTION";
    public static final String AUCTION_END   = "AUCTION_END";
    public static final String GET_AUCTIONS  = "GET_AUCTIONS";
    public static final String AUCTION_LIST  = "AUCTION_LIST";
    public static final String ERROR         = "ERROR";

    private static final Gson gson = new Gson();

    public Message() {}

    public Message(String type, String payload, String senderId) {
        this.type      = type;
        this.payload   = payload;
        this.senderId  = senderId;
        this.timestamp = System.currentTimeMillis();
    }
// Hàm toJson: đóng gói để gửi. Dùng khi muốn guiwr tin nhắn qua mạng
    public String toJson() {
        return gson.toJson(this);
    }
//Hàm fromJson: mowr gói khi nhận
    public static Message fromJson(String json) {
        return gson.fromJson(json, Message.class);
    }

    // Getters
    public String getType()     { return type; }
    public String getPayload()  { return payload; }
    public String getSenderId() { return senderId; }
    public long getTimestamp()  { return timestamp; }

    @Override
    public String toString() {
        return "[" + type + "] from=" + senderId + " payload=" + payload;
    }
}