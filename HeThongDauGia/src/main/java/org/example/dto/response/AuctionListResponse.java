package org.example.dto.response;

import java.util.List;

/**
 * JSON output:
 * {"status":"LIST_SUCCESS","message":"Danh sach phien dau gia",
 *  "count":2,
 *  "auctions":[
 *    {"id":3,"name":"Tranh Son Dau","currentPrice":5000000,"status":"RUNNING"},
 *    {"id":7,"name":"iPhone 15","currentPrice":20000000,"status":"RUNNING"}
 *  ]}
 */
public class AuctionListResponse extends BaseResponse {

    private final int                  count;
    private final List<AuctionSummary> auctions;

    public AuctionListResponse(List<AuctionSummary> auctions) {
        super(STATUS_LIST, "Danh sach phien dau gia");
        this.auctions = auctions;
        this.count    = auctions.size();
    }

    public int                  getCount()    { return count; }
    public List<AuctionSummary> getAuctions() { return auctions; }
}