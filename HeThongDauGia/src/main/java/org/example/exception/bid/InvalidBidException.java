package org.example.exception.bid;

import org.example.exception.AuctionSystemException;

import java.math.BigDecimal;

public class InvalidBidException extends AuctionSystemException {
    public InvalidBidException(BigDecimal currentPrice) {
        super("Giá đặt không hợp lệ! Giá hiện tại đang là: " + currentPrice);
    }

    public InvalidBidException(String message) {
        super(message);
    }
}
