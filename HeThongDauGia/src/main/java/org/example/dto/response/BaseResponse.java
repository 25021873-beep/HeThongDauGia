package org.example.dto.response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Base wrapper cho mọi response gửi về client qua Socket - JSON format.
 *
 * Format JSON chuẩn (1 dòng duy nhất để readLine() hoạt động):
 * {"status":"SUCCESS","message":"...","data":{...}}
 *
 * Gson tự động serialize tất cả field kể cả của subclass
 * vì serialize() gọi GSON.toJson(this) với runtime type thực tế.
 */
public abstract class BaseResponse {

    public static final String STATUS_SUCCESS     = "SUCCESS";
    public static final String STATUS_ERROR       = "ERROR";
    public static final String STATUS_INFO        = "INFO";
    public static final String STATUS_UPDATE      = "UPDATE";
    public static final String STATUS_LIST        = "LIST_SUCCESS";
    public static final String STATUS_AUCTION_END = "AUCTION_END";
    public static final String STATUS_AUCTION_STARTED = "AUCTION_STARTED";

    // Gson instance dùng chung - thread-safe
    static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create(); // KHÔNG dùng prettyPrinting: phải gửi 1 dòng qua socket

    protected final String status;
    protected final String message;

    protected BaseResponse(String status, String message) {
        this.status  = status;
        this.message = message;
    }

    public String  getStatus()  { return status; }
    public String  getMessage() { return message; }
    public boolean isSuccess()  { return STATUS_SUCCESS.equals(status); }

    /**
     * Serialize thành JSON 1 dòng để gửi qua socket.
     * Gson dùng runtime type (getClass()) nên field của subclass
     * được include đầy đủ.
     */
    public String serialize() {
        return GSON.toJson(this);
    }

    @Override
    public String toString() {
        return serialize();
    }
}