package org.example.dto.response;

import java.time.LocalDateTime;

/**
 * Gửi tới tất cả client trong phòng khi phiên bị gia hạn do anti-snipe.
 * Client nhận type = "AUCTION_EXTENDED" để hiển thị đồng hồ đếm ngược mới.
 */
public class AuctionExtendedResponse extends BaseResponse {

    private final int           auctionId;
    private final int           addedSeconds;
    private final LocalDateTime newEndTime;

    public AuctionExtendedResponse(int auctionId, int addedSeconds, LocalDateTime newEndTime) {
        super("AUCTION_EXTENDED", "Phien dau gia da duoc gia han them " + addedSeconds + " giay");
        this.auctionId    = auctionId;
        this.addedSeconds = addedSeconds;
        this.newEndTime   = newEndTime;
    }

    public int           getAuctionId()    { return auctionId; }
    public int           getAddedSeconds() { return addedSeconds; }
    public LocalDateTime getNewEndTime()   { return newEndTime; }
}