package com.example.doan;

import java.util.Date;

public class ChatPreview {
    private String userId;
    private String name;

    private String avatarUrl;
    private String lastMessage;
    private Date timestamp;

    public ChatPreview() {}

    public ChatPreview(String userId, String name, String avatarUrl, String lastMessage, Date timestamp) {
        this.userId = userId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.lastMessage = lastMessage;
        this.timestamp = timestamp;
    }

    // Getter và Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUri(String avatarUri) { this.avatarUrl = avatarUri; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
