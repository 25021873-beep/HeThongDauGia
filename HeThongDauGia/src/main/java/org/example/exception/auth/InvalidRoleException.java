package org.example.exception.auth;

import org.example.exception.AuctionSystemException;

public class InvalidRoleException extends AuctionSystemException {
    public InvalidRoleException(String message){
        super(message);
    }
}
