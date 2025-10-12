package com.example.popflix.model;

public class Movie {
    private String title;
    private String poster_path;
    private String overview;
    // Add other fields like id, release_date, etc., as needed.

    // Getters and setters for the fields
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return poster_path;
    }

    public void setPosterPath(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }
}