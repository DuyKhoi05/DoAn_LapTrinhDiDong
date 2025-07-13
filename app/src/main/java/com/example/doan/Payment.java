package com.example.doan;

import java.util.Date;

public class Payment {
    private String transactionId;
    private String date;
    private String itemName;
    private double amount;
    private String status;
    private String userId;
    private String sellerId;

    public Payment() {
        // Bắt buộc phải có constructor rỗng cho Firestore
    }

    public Payment(String transactionId, String date, String itemName, double amount, String status, String userId, String sellerId) {
        this.transactionId = transactionId;
        this.date = date;
        this.itemName = itemName;
        this.amount = amount;
        this.status = status;
        this.userId = userId;
        this.sellerId = sellerId;
    }

    // Getter
    public String getTransactionId() {
        return transactionId;
    }

    public String getDate() {
        return date;
    }

    public String getItemName() {
        return itemName;
    }

    public double getAmount() {
        return amount;
    }

    public String getStatus() {
        return status;
    }

    public String getUserId() {
        return userId;
    }

    public String getSellerId() {
        return sellerId;
    }

    // Setter
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
}