package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.Map;

public class Cart {
    /*************************************************************VARIABLES******************************************************************************************/
    private String userId;

    private Map<String, OrderItem> items;

    private double total;

    public Cart() {
        this.items = new HashMap<>();
    }

    public Cart(String userId) {
        this();
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(Map<String, OrderItem> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public void addItem(OrderItem item) {
        items.put(item.getProductId(), item);
        calculateTotal();
    }

    public void removeItem(String productId) {
        items.remove(productId);
        calculateTotal();
    }

    public void calculateTotal() {
        total = items.values().stream()
                .mapToDouble(OrderItem::getTotalPrice)
                .sum();
    }
}
