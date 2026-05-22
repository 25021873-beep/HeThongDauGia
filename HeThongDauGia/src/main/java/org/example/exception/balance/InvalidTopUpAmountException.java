package org.example.exception.balance;

import org.example.exception.AuctionSystemException;

public class InvalidTopUpAmountException extends AuctionSystemException {
    public InvalidTopUpAmountException(String message){
        super(message);
    }
}
