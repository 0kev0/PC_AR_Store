package com.example.pcarstore.ModelsDB;

import java.util.Date;

public class WishlistItem {
    private String userId;
    private String productId;
    private Date addedDate;
    private String note;

    public WishlistItem() {
    }

    public WishlistItem(String userId, String productId, Date addedDate, String note) {
        this.userId = userId;
        this.productId = productId;
        this.addedDate = addedDate;
        this.note = note;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
