package com.example.pcarstore.ModelsDB;

public class UserSales {
    private String userId;
    private String userName;
    private int orderCount;
    private double totalAmount;

    public UserSales() {}

    public UserSales(String userId) {
        this.userId = userId;
        this.orderCount = 0;
        this.totalAmount = 0;
    }

    public void addOrder(double amount) {
        this.orderCount++;
        this.totalAmount += amount;
    }

    public String getUserId() { return userId; }

    public int getOrderCount() { return orderCount; }

    public double getTotalAmount() { return totalAmount; }

    public void setUserName(String userName) {this.userName = userName;}

    public String getUserName() {return userName != null ? userName : userId;}
}