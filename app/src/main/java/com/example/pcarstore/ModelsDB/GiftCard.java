package com.example.pcarstore.ModelsDB;

import com.google.firebase.database.DataSnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GiftCard {
    private String cardId;
    private String code;
    private double amount;
    private String currency;
    private Date creationDate;
    private Date expirationDate;
    private String status;
    private String recipientEmail;
    private String createdBy;
    private String redeemedBy;
    private Date redeemedDate;

    public GiftCard() {}

    public GiftCard(String code, double amount, String createdBy) {
        this.code = code;
        this.amount = amount;
        this.currency = "USD";
        this.creationDate = new Date();
        this.expirationDate = new Date(System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000)); // 1 año de validez
        this.status = "active";
        this.createdBy = createdBy;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getRedeemedBy() {
        return redeemedBy;
    }

    public void setRedeemedBy(String redeemedBy) {
        this.redeemedBy = redeemedBy;
    }

    public Date getRedeemedDate() {
        return redeemedDate;
    }

    public void setRedeemedDate(Date redeemedDate) {
        this.redeemedDate = redeemedDate;
    }

    // Método para generar códigos de Gift Card (puede moverse a un servicio)
    public static String generateGiftCardCode() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder("PDM-");

        for (int i = 0; i < 6; i++) {
            int index = (int)(Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    // Método para verificar si la tarjeta es válida
    public boolean isValid() {
        return "active".equals(status) && new Date().before(expirationDate);
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("cardId", cardId);
        result.put("code", code);
        result.put("amount", amount);
        result.put("currency", currency);
        result.put("creationDate", creationDate != null ? creationDate.getTime() : null);
        result.put("expirationDate", expirationDate != null ? expirationDate.getTime() : null);
        result.put("status", status);
        result.put("createdBy", createdBy);
        result.put("recipientEmail", recipientEmail);
        result.put("redeemedBy", redeemedBy);
        result.put("redeemedDate", redeemedDate != null ? redeemedDate.getTime() : null);
        return result;
    }

    public static GiftCard fromSnapshot(DataSnapshot snapshot) {
        GiftCard giftCard = new GiftCard();
        giftCard.setCardId(snapshot.child("cardId").getValue(String.class));
        giftCard.setCode(snapshot.child("code").getValue(String.class));
        giftCard.setAmount(snapshot.child("amount").getValue(Double.class));
        giftCard.setCurrency(snapshot.child("currency").getValue(String.class));
        giftCard.setStatus(snapshot.child("status").getValue(String.class));
        giftCard.setCreatedBy(snapshot.child("createdBy").getValue(String.class));
        giftCard.setRecipientEmail(snapshot.child("recipientEmail").getValue(String.class));
        giftCard.setRedeemedBy(snapshot.child("redeemedBy").getValue(String.class));

        // Conversión de timestamps a Date
        Long creationTimestamp = snapshot.child("creationDate").getValue(Long.class);
        if (creationTimestamp != null) {
            giftCard.setCreationDate(new Date(creationTimestamp));
        }

        Long expirationTimestamp = snapshot.child("expirationDate").getValue(Long.class);
        if (expirationTimestamp != null) {
            giftCard.setExpirationDate(new Date(expirationTimestamp));
        }

        Long redeemedTimestamp = snapshot.child("redeemedDate").getValue(Long.class);
        if (redeemedTimestamp != null) {
            giftCard.setRedeemedDate(new Date(redeemedTimestamp));
        }

        return giftCard;
    }

}