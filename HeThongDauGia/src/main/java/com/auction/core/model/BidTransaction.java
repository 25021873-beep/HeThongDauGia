package com.auction.core.model;

import java.time.LocalDateTime;

public class BidTransaction {
    // Sử dụng từ khóa 'final' để đảm bảo tính bất biến (Immutable).
    // Khi một giao dịch đã được tạo ra, không ai có thể sửa lại thông tin của nó.
    private final Bidder bidder;
    private final double bidAmount;
    private final LocalDateTime timestamp;

    public BidTransaction(Bidder bidder, double bidAmount, LocalDateTime timestamp) {
        this.bidder = bidder;
        this.bidAmount = bidAmount;
        this.timestamp = timestamp;
    }

    // Chỉ cung cấp hàm Get, KHÔNG cung cấp hàm Set (để chống gian lận sửa giá)
    public Bidder getBidder() { return bidder; }
    public double getBidAmount() { return bidAmount; }
    public LocalDateTime getTimestamp() { return timestamp; }
}
