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