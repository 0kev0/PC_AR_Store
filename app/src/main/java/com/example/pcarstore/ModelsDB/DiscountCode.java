package com.example.pcarstore.ModelsDB;

import java.util.Date;

public class DiscountCode {
    /*************************************************************VARIABLES******************************************************************************************/
    private String codeId;
    private String code;
    private double discountPercentage; // 15.0 = 15%
    private Date creationDate;
    private Date expirationDate;
    private String status; // "active", "used", "expired"
    private String createdBy;
    private String usedBy;
    private Date usedDate;
    private double minPurchaseRequired;

    public DiscountCode() {}

    public DiscountCode(String code, double discountPercentage, String createdBy) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.createdBy = createdBy;
        this.creationDate = new Date();
        this.expirationDate = new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)); // 30 días de validez
        this.status = "active";
        this.minPurchaseRequired = 0.0;
    }

    public String getCodeId() { return codeId; }
    public void setCodeId(String codeId) { this.codeId = codeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public Date getCreationDate() { return creationDate; }
    public void setCreationDate(Date creationDate) { this.creationDate = creationDate; }

    public Date getExpirationDate() { return expirationDate; }
    public void setExpirationDate(Date expirationDate) { this.expirationDate = expirationDate; }

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUsedBy() { return usedBy; }
    public void setUsedBy(String usedBy) { this.usedBy = usedBy; }

    public Date getUsedDate() { return usedDate; }
    public void setUsedDate(Date usedDate) { this.usedDate = usedDate; }

    public double getMinPurchaseRequired() { return minPurchaseRequired; }
    public void setMinPurchaseRequired(double minPurchaseRequired) { this.minPurchaseRequired = minPurchaseRequired; }

    // Metodo de validación
    public boolean isValid() {
        return "active".equals(status) &&
                new Date().before(expirationDate) &&
                usedBy == null;
    }
    public static String generateDiscountCode() {
        String characters = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder code = new StringBuilder("PDM-");

        for (int i = 0; i < 6; i++) {
            int index = (int)(Math.random() * characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }

    // Metodo para marcar como usado
    public void markAsUsed(String userId) {
        this.status = "used";
        this.usedBy = userId;
        this.usedDate = new Date();
    }
}