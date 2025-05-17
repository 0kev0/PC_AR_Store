package com.example.pcarstore.ModelsDB;


import java.util.HashMap;
import java.util.Map;

public class Wishlist {
    private String userId;
    private Map<String, OrderItem> items;

    public Wishlist() {
        this.items = new HashMap<>();
    }

    public Wishlist(String userId) {
        this();
        this.userId = userId;
    }

    public Map<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(Map<String, OrderItem> items) {
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // Getters y setters

    public void addItem(OrderItem item) {
        items.put(item.getProductId(), item);
    }

    public void moveToCart(String productId, Cart cart) {
        OrderItem item = items.get(productId);
        if (item != null) {
            cart.addItem(item);
            items.remove(productId);
        }
    }
}