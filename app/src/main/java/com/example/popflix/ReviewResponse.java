package com.example.popflix;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ReviewResponse {
    @SerializedName("id")
    private int id;
    
    @SerializedName("page")
    private int page;
    
    @SerializedName("results")
    private List<TMDBReview> results;
    
    @SerializedName("total_pages")
    private int totalPages;
    
    @SerializedName("total_results")
    private int totalResults;
    
    // Getters and setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getPage() {
        return page;
    }
    
    public void setPage(int page) {
        this.page = page;
    }
    
    public List<TMDBReview> getResults() {
        return results;
    }
    
    public void setResults(List<TMDBReview> results) {
        this.results = results;
    }
    
    public int getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }
    
    // TMDB Review class
    public static class TMDBReview {
        @SerializedName("author")
        private String author;
        
        @SerializedName("author_details")
        private AuthorDetails authorDetails;
        
        @SerializedName("content")
        private String content;
        
        @SerializedName("created_at")
        private String createdAt;
        
        @SerializedName("id")
        private String id;
        
        @SerializedName("updated_at")
        private String updatedAt;
        
        @SerializedName("url")
        private String url;
        
        // Getters and setters
        public String getAuthor() {
            return author;
        }
        
        public void setAuthor(String author) {
            this.author = author;
        }
        
        public AuthorDetails getAuthorDetails() {
            return authorDetails;
        }
        
        public void setAuthorDetails(AuthorDetails authorDetails) {
            this.authorDetails = authorDetails;
        }
        
        public String getContent() {
            return content;
        }
        
        public void setContent(String content) {
            this.content = content;
        }
        
        public String getCreatedAt() {
            return createdAt;
        }
        
        public void setCreatedAt(String createdAt) {
            this.createdAt = createdAt;
        }
        
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getUpdatedAt() {
            return updatedAt;
        }
        
        public void setUpdatedAt(String updatedAt) {
            this.updatedAt = updatedAt;
        }
        
        public String getUrl() {
            return url;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
    }
    
    // Author Details class
    public static class AuthorDetails {
        @SerializedName("name")
        private String name;
        
        @SerializedName("username")
        private String username;
        
        @SerializedName("avatar_path")
        private String avatarPath;
        
        @SerializedName("rating")
        private Double rating;
        
        // Getters and setters
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getAvatarPath() {
            return avatarPath;
        }
        
        public void setAvatarPath(String avatarPath) {
            this.avatarPath = avatarPath;
        }
        
        public Double getRating() {
            return rating;
        }
        
        public void setRating(Double rating) {
            this.rating = rating;
        }
    }
}
