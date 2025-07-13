package com.example.doan;

public class Product {
    private String id; // document ID từ Firestore
    private String title;
    private String description;
    private double price;
    private String imageUrl;
    private String category;
    private String ownerId;

    private Long views;

    private String status = "Available";

    public Product() {
        // Constructor mặc định cho Firebase
    }

    public String getOwnerId() {
        return ownerId;
    }
    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
    public Long getViews() {
        return views;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public Product(String title, String description, double price, String imageUrl) {
        this.title = title;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }


    // Getter & Setter cho id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Giữ nguyên các getter gốc
    public String getTitle() {
        return title;
    }

    public void setViews(Long views) {
        this.views = views;
    }

    public String getDescription() {
        return description;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // (Không bắt buộc) Có thể thêm setter nếu cần cập nhật dữ liệu
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
