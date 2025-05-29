package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.Map;

public class User {
    /*************************************************************VARIABLES******************************************************************************************/
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

    private User(Builder builder) {
        this.email = builder.email;
        this.name = builder.name;
        this.role = builder.role;
        this.profileImageUrl = builder.profileImageUrl;
        this.departamento = builder.departamento;
        this.ciudad = builder.ciudad;
        this.saldo = builder.saldo;
        this.membresiaPrime = builder.membresiaPrime;
        this.cart = builder.cart != null ? builder.cart : new HashMap<>();
    }

    public User() {
    }

    // Builder class
    public static class Builder {
        private final String email;
        private String name = "";
        private String role = "client";
        private String profileImageUrl = "";
        private String departamento = "";
        private String ciudad = "";
        private double saldo = 0.0;
        private boolean membresiaPrime = false;
        private Map<String, Integer> cart = new HashMap<>();

        public Builder(String email) {
            this.email = email;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setRole(String role) {
            this.role = role;
            return this;
        }

        public Builder setProfileImageUrl(String profileImageUrl) {
            this.profileImageUrl = profileImageUrl;
            return this;
        }

        public Builder setDepartamento(String departamento) {
            this.departamento = departamento;
            return this;
        }

        public Builder setCiudad(String ciudad) {
            this.ciudad = ciudad;
            return this;
        }

        public Builder setSaldo(double saldo) {
            this.saldo = saldo;
            return this;
        }

        public Builder setMembresiaPrime(boolean membresiaPrime) {
            this.membresiaPrime = membresiaPrime;
            return this;
        }

        public Builder setCart(Map<String, Integer> cart) {
            this.cart = cart;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    // Getters y setters...
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
    public void agregarSaldo(double cantidad) {
        if (cantidad > 0) {
            this.saldo += cantidad;
        }
    }
}