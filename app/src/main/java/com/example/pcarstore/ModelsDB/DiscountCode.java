package com.example.pcarstore.ModelsDB;

import com.google.firebase.database.Exclude;
import java.util.Date;

public class DiscountCode {
    /*************************************************************VARIABLES******************************************************************************************/
    private String codeId;
    private String code;
    private double discountPercentage; // 15.0 = 15%

    @Exclude
    private Date creationDate;
    private Long creationDateTimestamp;

    @Exclude
    private Date expirationDate;
    private Long expirationDateTimestamp;

    private String status; // "active", "used", "expired"
    private String createdBy;
    private String usedBy;

    @Exclude
    private Date usedDate;
    private Long usedDateTimestamp;

    private double minPurchaseRequired;

    public DiscountCode() {}

    public DiscountCode(String code, double discountPercentage, String createdBy) {
        this.code = code;
        this.discountPercentage = discountPercentage;
        this.createdBy = createdBy;
        setCreationDate(new Date());
        setExpirationDate(new Date(System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000))); // 30 días de validez
        this.status = "active";
        this.minPurchaseRequired = 0.0;
    }

    // Getters y Setters normales
    public String getCodeId() { return codeId; }
    public void setCodeId(String codeId) { this.codeId = codeId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public double getDiscountPercentage() { return discountPercentage; }
    public void setDiscountPercentage(double discountPercentage) { this.discountPercentage = discountPercentage; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUsedBy() { return usedBy; }
    public void setUsedBy(String usedBy) { this.usedBy = usedBy; }

    public double getMinPurchaseRequired() { return minPurchaseRequired; }
    public void setMinPurchaseRequired(double minPurchaseRequired) { this.minPurchaseRequired = minPurchaseRequired; }

    // Getters y Setters para fechas (compatibilidad con Firebase)
    @Exclude
    public Date getCreationDate() {
        if (creationDate == null && creationDateTimestamp != null) {
            creationDate = new Date(creationDateTimestamp);
        }
        return creationDate;
    }

    @Exclude
    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
        this.creationDateTimestamp = creationDate != null ? creationDate.getTime() : null;
    }

    public Long getCreationDateTimestamp() {
        if (creationDateTimestamp == null && creationDate != null) {
            creationDateTimestamp = creationDate.getTime();
        }
        return creationDateTimestamp;
    }

    public void setCreationDateTimestamp(Long creationDateTimestamp) {
        this.creationDateTimestamp = creationDateTimestamp;
        this.creationDate = creationDateTimestamp != null ? new Date(creationDateTimestamp) : null;
    }

    @Exclude
    public Date getExpirationDate() {
        if (expirationDate == null && expirationDateTimestamp != null) {
            expirationDate = new Date(expirationDateTimestamp);
        }
        return expirationDate;
    }

    @Exclude
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
        this.expirationDateTimestamp = expirationDate != null ? expirationDate.getTime() : null;
    }

    public Long getExpirationDateTimestamp() {
        if (expirationDateTimestamp == null && expirationDate != null) {
            expirationDateTimestamp = expirationDate.getTime();
        }
        return expirationDateTimestamp;
    }

    public void setExpirationDateTimestamp(Long expirationDateTimestamp) {
        this.expirationDateTimestamp = expirationDateTimestamp;
        this.expirationDate = expirationDateTimestamp != null ? new Date(expirationDateTimestamp) : null;
    }

    @Exclude
    public Date getUsedDate() {
        if (usedDate == null && usedDateTimestamp != null) {
            usedDate = new Date(usedDateTimestamp);
        }
        return usedDate;
    }

    @Exclude
    public void setUsedDate(Date usedDate) {
        this.usedDate = usedDate;
        this.usedDateTimestamp = usedDate != null ? usedDate.getTime() : null;
    }

    public Long getUsedDateTimestamp() {
        if (usedDateTimestamp == null && usedDate != null) {
            usedDateTimestamp = usedDate.getTime();
        }
        return usedDateTimestamp;
    }

    public void setUsedDateTimestamp(Long usedDateTimestamp) {
        this.usedDateTimestamp = usedDateTimestamp;
        this.usedDate = usedDateTimestamp != null ? new Date(usedDateTimestamp) : null;
    }

    // Metodo de validación
    public boolean isValid() {
        Date now = new Date();
        return "active".equals(status) &&
                (getExpirationDate() == null || now.before(getExpirationDate())) &&
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
        setUsedDate(new Date());
    }
}