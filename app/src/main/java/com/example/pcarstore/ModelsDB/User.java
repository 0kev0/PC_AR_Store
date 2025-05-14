package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role;
    private Map<String, Integer> cart;

    public User() {}

    public User(String email, String name, String role) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.cart = new HashMap<>();
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public Map<String, Integer> getCart() { return cart; }
    public void setCart(Map<String, Integer> cart) { this.cart = cart; }
}
