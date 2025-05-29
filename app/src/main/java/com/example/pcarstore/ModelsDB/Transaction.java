package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Transaction {
    /*************************************************************VARIABLES******************************************************************************************/
    private String id;
    private String type;
    private double amount;
    private String description;
    private long date;
    private String status;
    private String userId;
    private List<ProductItem> products;
    private double shippingCost;
    private String discountCode;
    private double discountAmount;

    // Constructor vacío
    public Transaction() {}

    public static class ProductItem {
        private String productId;
        private int quantity;
        private double price;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getDate() { return date; }
    public void setDate(long date) { this.date = date; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReadableType() {
        switch (type) {
            case "giftcard_purchase": return "Compra de Gift Card";
            case "sale": return "Venta";
            case "expense": return "Gasto";
            default: return type;
        }
    }

    public void setTimestamp(Long date) {
    }
}