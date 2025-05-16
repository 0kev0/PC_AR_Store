package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.Map;

public class User {
    private String userId;
    private String email;
    private String name;
    private String role;
    private String profileImageUrl;
    private String departamento;
    private String ciudad;
    private double saldo;
    private boolean membresiaPrime;
    private Map<String, Integer> cart;

    public User() {}

    public User(String email, String name, String role, String profileImageUrl,
                String departamento, String ciudad, double saldo, boolean membresiaPrime) {
        this.email = email;
        this.name = name;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.departamento = departamento;
        this.ciudad = ciudad;
        this.saldo = saldo;
        this.membresiaPrime = membresiaPrime;
        this.cart = new HashMap<>();
    }

    // Getters y Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getDepartamento() { return departamento; }
    public void setDepartamento(String departamento) { this.departamento = departamento; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public double getSaldo() { return saldo; }
    public void setSaldo(double saldo) { this.saldo = saldo; }

    public boolean isMembresiaPrime() { return membresiaPrime; }
    public void setMembresiaPrime(boolean membresiaPrime) { this.membresiaPrime = membresiaPrime; }

    public Map<String, Integer> getCart() { return cart; }
    public void setCart(Map<String, Integer> cart) { this.cart = cart; }
}