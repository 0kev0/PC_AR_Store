package com.example.pcarstore.ModelsDB;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.Exclude;
import com.google.firebase.database.PropertyName;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GiftCard {
    /*************************************************************VARIABLES******************************************************************************************/
    private String cardId;
    private String code;
    private double amount;
    private String currency = "USD";
    private Long creationDate;
    private Long expirationDate;
    private Long redeemedDate;
    private String status = "active";
    private String recipientEmail;
    private String createdBy;
    private String redeemedBy;

    public GiftCard() {}

    public GiftCard(double amount, String createdBy) {
        this.code = generateGiftCardCode();
        this.amount = amount;
        this.createdBy = createdBy;
        this.creationDate = System.currentTimeMillis();
        this.expirationDate = System.currentTimeMillis() + (365L * 24 * 60 * 60 * 1000); // 1 año
    }

    public GiftCard(String s, double amount, String userId) {
    }

    @PropertyName("cardId")
    public String getCardId() { return cardId; }

    @PropertyName("cardId")
    public void setCardId(String cardId) { this.cardId = cardId; }

    @PropertyName("code")
    public String getCode() { return code; }

    @PropertyName("code")
    public void setCode(String code) { this.code = code; }

    @PropertyName("amount")
    public double getAmount() { return amount; }

    @PropertyName("amount")
    public void setAmount(double amount) { this.amount = amount; }

    @PropertyName("currency")
    public String getCurrency() { return currency; }

    @PropertyName("currency")
    public void setCurrency(String currency) { this.currency = currency; }
    @Exclude
    public Date getCreationDate() {
        return creationDate != null ? new Date(creationDate) : null;
    }

    @Exclude
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate != null ? creationDate.getTime() : null;
    }

    @Exclude
    public Date getExpirationDate() {
        return expirationDate != null ? new Date(expirationDate) : null;
    }

    @Exclude
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate != null ? expirationDate.getTime() : null;
    }

    @Exclude
    public Date getRedeemedDate() {
        return redeemedDate != null ? new Date(redeemedDate) : null;
    }

    @Exclude
    public void setRedeemedDate(Date redeemedDate) {
        this.redeemedDate = redeemedDate != null ? redeemedDate.getTime() : null;
    }

    // Getters/Setters para campos almacenados
    @PropertyName("creationDate")
    public Long getCreationDateTimestamp() { return creationDate; }

    @PropertyName("creationDate")
    public void setCreationDateTimestamp(Long creationDate) {
        this.creationDate = creationDate;
    }

    @PropertyName("expirationDate")
    public Long getExpirationDateTimestamp() { return expirationDate; }

    @PropertyName("expirationDate")
    public void setExpirationDateTimestamp(Long expirationDate) {
        this.expirationDate = expirationDate;
    }

    @PropertyName("redeemedDate")
    public Long getRedeemedDateTimestamp() { return redeemedDate; }

    @PropertyName("redeemedDate")
    public void setRedeemedDateTimestamp(Long redeemedDate) {
        this.redeemedDate = redeemedDate;
    }

    @PropertyName("status")
    public String getStatus() { return status; }

    @PropertyName("status")
    public void setStatus(String status) { this.status = status; }

    @PropertyName("recipientEmail")
    public String getRecipientEmail() { return recipientEmail; }

    @PropertyName("recipientEmail")
    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    @PropertyName("createdBy")
    public String getCreatedBy() { return createdBy; }

    @PropertyName("createdBy")
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    @PropertyName("redeemedBy")
    public String getRedeemedBy() { return redeemedBy; }

    @PropertyName("redeemedBy")
    public void setRedeemedBy(String redeemedBy) { this.redeemedBy = redeemedBy; }

    // -------------------------------
    // Métodos de negocio
    // -------------------------------

    @Exclude
    public boolean isValid() {
        return "active".equals(status) && !isExpired();
    }

    @Exclude
    public boolean isExpired() {
        return expirationDate != null && System.currentTimeMillis() > expirationDate;
    }

    @Exclude
    public static String generateGiftCardCode() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder("PDM-");

        for (int i = 0; i < 6; i++) {
            int index = (int)(Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    // -------------------------------
    // Conversión Firebase
    // -------------------------------

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("cardId", cardId);
        result.put("code", code);
        result.put("amount", amount);
        result.put("currency", currency);
        result.put("creationDate", creationDate);
        result.put("expirationDate", expirationDate);
        result.put("status", status);
        result.put("createdBy", createdBy);
        result.put("recipientEmail", recipientEmail);
        result.put("redeemedBy", redeemedBy);
        result.put("redeemedDate", redeemedDate);
        return result;
    }

    @Exclude
    public static GiftCard fromSnapshot(DataSnapshot snapshot) {
        GiftCard card = new GiftCard();
        card.setCardId(snapshot.getKey());

        // Mapeo seguro de campos
        if (snapshot.hasChild("code")) card.setCode(snapshot.child("code").getValue(String.class));
        if (snapshot.hasChild("amount")) card.setAmount(snapshot.child("amount").getValue(Double.class));
        if (snapshot.hasChild("currency")) card.setCurrency(snapshot.child("currency").getValue(String.class));
        if (snapshot.hasChild("status")) card.setStatus(snapshot.child("status").getValue(String.class));
        if (snapshot.hasChild("createdBy")) card.setCreatedBy(snapshot.child("createdBy").getValue(String.class));
        if (snapshot.hasChild("recipientEmail")) card.setRecipientEmail(snapshot.child("recipientEmail").getValue(String.class));
        if (snapshot.hasChild("redeemedBy")) card.setRedeemedBy(snapshot.child("redeemedBy").getValue(String.class));

        // Manejo de fechas
        if (snapshot.hasChild("creationDate")) {
            Long ts = snapshot.child("creationDate").getValue(Long.class);
            card.setCreationDateTimestamp(ts);
        }

        if (snapshot.hasChild("expirationDate")) {
            Long ts = snapshot.child("expirationDate").getValue(Long.class);
            card.setExpirationDateTimestamp(ts);
        }

        if (snapshot.hasChild("redeemedDate")) {
            Long ts = snapshot.child("redeemedDate").getValue(Long.class);
            card.setRedeemedDateTimestamp(ts);
        }

        // Auto-actualizar estado si está expirado
        if ("active".equals(card.getStatus()) && card.isExpired()) {
            card.setStatus("expired");
        }

        return card;
    }
}