package com.example.doan;

import java.util.Date;

public class Review {
    private String userId;
    private float rating;
    private String comment;
    private Date timestamp;

    public Review() {}

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public float getRating() { return rating; }
    public void setRating(float rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}
