package com.example.popflix;

import com.google.firebase.firestore.PropertyName;

public class Review {
    private String id;
    private String userId;
    private String userName;
    private int movieId;
    private String movieTitle;
    private float rating;
    private String content;
    private long timestamp;

    public Review() {
        // Default constructor for Firestore
    }

    public Review(String userId, String userName, int movieId, String movieTitle, float rating, String content) {
        this.userId = userId;
        this.userName = userName;
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.rating = rating;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    @PropertyName("id")
    public String getId() {
        return id;
    }

    @PropertyName("id")
    public void setId(String id) {
        this.id = id;
    }

    @PropertyName("userId")
    public String getUserId() {
        return userId;
    }

    @PropertyName("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    @PropertyName("userName")
    public String getUserName() {
        return userName;
    }

    @PropertyName("userName")
    public void setUserName(String userName) {
        this.userName = userName;
    }

    @PropertyName("movieId")
    public int getMovieId() {
        return movieId;
    }

    @PropertyName("movieId")
    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    @PropertyName("movieTitle")
    public String getMovieTitle() {
        return movieTitle;
    }

    @PropertyName("movieTitle")
    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    @PropertyName("rating")
    public float getRating() {
        return rating;
    }

    @PropertyName("rating")
    public void setRating(float rating) {
        this.rating = rating;
    }

    @PropertyName("content")
    public String getContent() {
        return content;
    }

    @PropertyName("content")
    public void setContent(String content) {
        this.content = content;
    }

    @PropertyName("timestamp")
    public long getTimestamp() {
        return timestamp;
    }

    @PropertyName("timestamp")
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFormattedDate() {
        // Simple date formatting - in a real app, you'd use a proper date formatter
        return new java.text.SimpleDateFormat("MMM dd, yyyy").format(new java.util.Date(timestamp));
    }
}
