package org.example.dto.response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AuctionStartedResponse extends BaseResponse {

    // Dùng chung một format thời gian y như BidResponse
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private final int auctionId;
    private final String auctionName;
    private final String startTime;

    public AuctionStartedResponse(int auctionId, String auctionName, LocalDateTime startTime) {
        // Gọi lên BaseResponse, nhét status và lời nhắn vào
        super(STATUS_AUCTION_STARTED, "Phòng đấu giá " + auctionName + " đã chính thức mở cửa!");

        this.auctionId = auctionId;
        this.auctionName = auctionName;
        // Parse thời gian ra String giống hệt cách m làm ở BidResponse
        this.startTime = startTime != null ? startTime.format(FMT) : null;
    }

    // Getters cho thư viện Gson nó tự động bốc dữ liệu để parse sang JSON
    public int getAuctionId() { return auctionId; }
    public String getAuctionName() { return auctionName; }
    public String getStartTime() { return startTime; }
}