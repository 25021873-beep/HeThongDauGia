package org.example.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BidResult {

    private final boolean       success;
    private final boolean       extended;
    private final int           extendedSeconds;
    private final LocalDateTime newEndTime;
    private final String        errorMessage;

    private BidResult(boolean success, boolean extended,
                      int extendedSeconds, LocalDateTime newEndTime,
                      String errorMessage) {
        this.success         = success;
        this.extended        = extended;
        this.extendedSeconds = extendedSeconds;
        this.newEndTime      = newEndTime;
        this.errorMessage    = errorMessage;
    }

    // ── Factory methods ───────────────────────────────────────────────────────

    public static BidResult success() {
        return new BidResult(true, false, 0, null, null);
    }

    public static BidResult successWithExtension(int seconds, LocalDateTime newEndTime) {
        return new BidResult(true, true, seconds, newEndTime, null);
    }

    public static BidResult failure(String message) {
        return new BidResult(false, false, 0, null, message);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public boolean       isSuccess()          { return success; }
    public boolean       isExtended()         { return extended; }
    public int           getExtendedSeconds() { return extendedSeconds; }
    public LocalDateTime getNewEndTime()      { return newEndTime; }
    public String        getErrorMessage()    { return errorMessage; }
}