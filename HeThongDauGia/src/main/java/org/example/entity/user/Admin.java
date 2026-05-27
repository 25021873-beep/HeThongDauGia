package org.example.entity.user;

public class Admin extends User {
    public Admin() {
        this.role = "ADMIN";
    }

    @Override
    public void doSomething() {
        System.out.println("T là Admin");
    }
}
