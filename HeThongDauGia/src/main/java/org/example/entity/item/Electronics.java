package org.example.entity.item;

public class Electronics extends Item {
    private int warrantyMonths;

    public Electronics() {
        this.itemType = "ELECTRONICS";
    }

    public int getWarrantyMonths() { return warrantyMonths; }
    public void setWarrantyMonths(int warrantyMonths) { this.warrantyMonths = warrantyMonths; }
}
