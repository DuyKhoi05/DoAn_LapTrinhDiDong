package com.example.doan;

import java.util.Date;

public class Message {
    private String id;
    private String senderId;
    private String receiverId;
    private String text;
    private String imageUri;
    private String chatId;
    private Date timestamp;
    private boolean isSeen;

    public Message() {
        // Required for Firebase
    }

    public Message(String id, String senderId, String receiverId, String text,
                   String imageUri, String chatId, Date timestamp, boolean isSeen) {
        this.id = id;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.imageUri = imageUri;
        this.chatId = chatId;
        this.timestamp = timestamp;
        this.isSeen = isSeen;
    }

    // Getters and setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return isSeen;
    }

    public void setSeen(boolean seen) {
        isSeen = seen;
    }
}
