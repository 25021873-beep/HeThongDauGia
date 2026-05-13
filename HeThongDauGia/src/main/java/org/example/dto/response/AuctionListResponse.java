package org.example.dto.response;

import java.util.List;

/**
 * Response trả về sau lệnh GET_ALL_AUCTIONS.
 * Thay thế cho StringBuilder thủ công trong ClientHandler.
 *
 * Serialize format:
 *   "LIST_SUCCESS|<count>|<id>:<name>:<price>:<status>|<id>:<name>:<price>:<status>|..."
 *
 * Ví dụ (2 phiên):
 *   "LIST_SUCCESS|2|3:Tranh Son Dau:5000000:ACTIVE|7:iPhone 15:20000000:ACTIVE"
 *
 * Nếu rỗng → dùng SimpleResponse.info("Hien khong co phien dau gia nao")
 */
public class AuctionListResponse extends BaseResponse {

    public static final String STATUS_LIST_SUCCESS = "LIST_SUCCESS";

    private final List<AuctionSummary> auctions;

    public AuctionListResponse(List<AuctionSummary> auctions) {
        super(STATUS_LIST_SUCCESS, "Danh sach phien dau gia");
        this.auctions = auctions;
    }

    public List<AuctionSummary> getAuctions() { return auctions; }
    public int getCount() { return auctions.size(); }

    // ── Serialize ─────────────────────────────────────────────────────────────

    @Override
    public String serialize() {
        StringBuilder sb = new StringBuilder(STATUS_LIST_SUCCESS);
        sb.append(DELIMITER).append(auctions.size());
        for (AuctionSummary a : auctions) {
            sb.append(DELIMITER).append(a.serialize());
        }
        return sb.toString();
    }
}