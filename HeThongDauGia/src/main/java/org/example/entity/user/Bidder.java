package org.example.entity.user;

import java.math.BigDecimal;

public class Bidder extends User {
    private BigDecimal balance;

    public Bidder() {
        this.role = "BIDDER";
    }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    @Override
    public void doSomething() {
        System.out.println("T là Bidder");
    }
}
