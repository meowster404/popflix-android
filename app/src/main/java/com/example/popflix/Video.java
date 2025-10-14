package com.example.popflix;

import com.google.gson.annotations.SerializedName;

public class Video {
    @SerializedName("id")
    private String id;

    @SerializedName("key")
    private String key;

    @SerializedName("name")
    private String name;

    @SerializedName("site")
    private String site;

    @SerializedName("type")
    private String type;

    @SerializedName("official")
    private boolean official;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("size")
    private int size;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isOfficial() {
        return official;
    }

    public void setOfficial(boolean official) {
        this.official = official;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public boolean isTrailer() {
        return "Trailer".equalsIgnoreCase(type);
    }

    public String getYouTubeUrl() {
        if ("YouTube".equalsIgnoreCase(site) && key != null) {
            return "https://www.youtube.com/watch?v=" + key;
        }
        return null;
    }
}
