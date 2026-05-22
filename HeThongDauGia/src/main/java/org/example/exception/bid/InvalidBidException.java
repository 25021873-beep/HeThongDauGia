package org.example.exception.bid;

import org.example.exception.AuctionSystemException;

import java.math.BigDecimal;

public class InvalidBidException extends AuctionSystemException {
    // Truyền thẳng giá hiện tại vào để nó tự render ra câu chửi
    public InvalidBidException(BigDecimal currentPrice) {
        super("Giá đặt không hợp lệ! Giá hiện tại đang là: " + currentPrice);
    }

    public InvalidBidException(String message) {
        super(message);
    }
}
