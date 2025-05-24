package com.example.pcarstore.ModelsDB;

import com.google.firebase.database.PropertyName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Calendar;

public class Order {
    private String orderId;
    private String userId;
    private Date date;
    private Date deliveryDate;
    private String status;
    private double total;
    private Map<String, OrderItem> items;

    public Order() {}

    public Order(String userId, String status, double total) {
        this.userId = userId;
        this.date = new Date();
        this.deliveryDate = calculateDeliveryDate(this.date);
        this.status = status;
        this.total = total;
        this.items = new HashMap<>();
    }

    private Date calculateDeliveryDate(Date purchaseDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(purchaseDate);
        calendar.add(Calendar.MINUTE, 1);
        return calendar.getTime();
    }


    // Getters y Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
        this.deliveryDate = calculateDeliveryDate(date);
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public Map<String, OrderItem> getItems() {
        return items;
    }

    public void setItems(Map<String, OrderItem> items) {
        this.items = items;
    }

}