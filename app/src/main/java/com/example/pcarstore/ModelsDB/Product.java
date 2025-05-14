package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.Map;

public class Product {
    private String productId;
    private String name;
    private double price;
    private String category;
    private int stock;
    private String description;
    private String imageUrl;
    private String model3dUrl;
    private Map<String, String> specs;

    public Product() {}

    public Product(String name, double price, String category, int stock,
                   String description, String imageUrl, String model3dUrl) {
        this.name = name;
        this.price = price;
        this.category = category;
        this.stock = stock;
        this.description = description;
        this.imageUrl = imageUrl;
        this.model3dUrl = model3dUrl;
        this.specs = new HashMap<>();
    }

    public String getProductId() {return productId;}

    public void setProductId(String productId) {this.productId = productId;}

    public String getName() {return name;}

    public void setName(String name) {this.name = name;}

    public double getPrice() {return price;}

    public void setPrice(double price) {this.price = price;}

    public String getCategory() {return category;}

    public void setCategory(String category) {this.category = category;}

    public int getStock() {return stock;}

    public void setStock(int stock) {this.stock = stock;}

    public String getDescription() {return description;}

    public void setDescription(String description) {this.description = description;}

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {this.imageUrl = imageUrl;}

    public String getModel3dUrl() {return model3dUrl;}

    public void setModel3dUrl(String model3dUrl) {this.model3dUrl = model3dUrl;}

    public Map<String, String> getSpecs() {return specs;}

    public void setSpecs(Map<String, String> specs) {this.specs = specs;}
}
