package com.example.pcarstore.ModelsDB;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Product {
    private String productId;
    private String name;
    private double price;
    private double cost;
    private int stock;
    private Double rating;
    private String category;
    private String description;
    private List<String> imageUrls;
    private String model3dUrl;
    private String textureUrl;
    private Map<String, String> specifications;
    private boolean inWishlist;

    public Product() {
        this.specifications = new HashMap<>();
    }

    public Product(String productId, String name, double price, double cost, int stock, Double rating, String category,
                   String description, List<String> imageUrls, String model3dUrl, String textureUrl, Map<String, String> specifications) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.rating = rating;
        this.category = category;
        this.description = description;
        this.imageUrls = imageUrls;
        this.model3dUrl = model3dUrl;
        this.textureUrl = textureUrl;
        this.specifications = specifications;
    }

    public boolean isInWishlist() {
        return inWishlist;
    }

    public void setInWishlist(boolean inWishlist) {
        this.inWishlist = inWishlist;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String getModel3dUrl() {
        return model3dUrl;
    }

    public void setModel3dUrl(String model3dUrl) {
        this.model3dUrl = model3dUrl;
    }

    public String getTextureUrl() {
        return textureUrl;
    }

    public void setTextureUrl(String textureUrl) {
        this.textureUrl = textureUrl;
    }

    public Map<String, String> getSpecifications() {
        return specifications;
    }

    public void setSpecifications(Map<String, String> specifications) {
        this.specifications = specifications != null ? specifications : new HashMap<>();
    }

    public void addSpecification(String key, String value) {
        this.specifications.put(key, value);
    }

    public void removeSpecification(String key) {
        this.specifications.remove(key);
    }

    public String getMainImageUrl() {
        return (imageUrls != null && !imageUrls.isEmpty()) ? imageUrls.get(0) : null;
    }

    public double getProfitMargin() {
        return price - cost;
    }


}