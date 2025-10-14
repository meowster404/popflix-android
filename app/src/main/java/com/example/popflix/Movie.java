package com.example.popflix;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Movie implements Serializable {
    @SerializedName("id")
    private int id;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("overview")
    private String overview;
    
    @SerializedName("poster_path")
    private String posterPath;
    
    @SerializedName("backdrop_path")
    private String backdropPath;
    
    @SerializedName("release_date")
    private String releaseDate;
    
    @SerializedName("vote_average")
    private double voteAverage;
    
    @SerializedName("vote_count")
    private int voteCount;
    
    @SerializedName("popularity")
    private double popularity;
    
    @SerializedName("adult")
    private boolean adult;
    
    @SerializedName("video")
    private boolean video;
    
    @SerializedName("original_language")
    private String originalLanguage;
    
    @SerializedName("original_title")
    private String originalTitle;
    
    @SerializedName("genre_ids")
    private List<Integer> genreIds;
    
    @SerializedName("runtime")
    private int runtime;
    
    @SerializedName("genres")
    private List<Genre> genres;
    
    @SerializedName("status")
    private String status;
    
    @SerializedName("tagline")
    private String tagline;
    
    @SerializedName("budget")
    private long budget;
    
    @SerializedName("revenue")
    private long revenue;
    
    @SerializedName("homepage")
    private String homepage;
    
    @SerializedName("imdb_id")
    private String imdbId;
    
    @SerializedName("production_companies")
    private List<ProductionCompany> productionCompanies;
    
    @SerializedName("production_countries")
    private List<ProductionCountry> productionCountries;
    
    @SerializedName("spoken_languages")
    private List<SpokenLanguage> spokenLanguages;

    // Constructors
    public Movie() {}

    public Movie(int id, String title, String overview, String posterPath, String backdropPath, 
                 String releaseDate, double voteAverage, int voteCount, double popularity, 
                 boolean adult, boolean video, String originalLanguage, String originalTitle, 
                 List<Integer> genreIds) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.backdropPath = backdropPath;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.popularity = popularity;
        this.adult = adult;
        this.video = video;
        this.originalLanguage = originalLanguage;
        this.originalTitle = originalTitle;
        this.genreIds = genreIds;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public double getPopularity() {
        return popularity;
    }

    public void setPopularity(double popularity) {
        this.popularity = popularity;
    }

    public boolean isAdult() {
        return adult;
    }

    public void setAdult(boolean adult) {
        this.adult = adult;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public List<Integer> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(List<Integer> genreIds) {
        this.genreIds = genreIds;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public List<Genre> getGenres() {
        return genres;
    }

    public void setGenres(List<Genre> genres) {
        this.genres = genres;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTagline() {
        return tagline;
    }

    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    public long getBudget() {
        return budget;
    }

    public void setBudget(long budget) {
        this.budget = budget;
    }

    public long getRevenue() {
        return revenue;
    }

    public void setRevenue(long revenue) {
        this.revenue = revenue;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }

    public List<ProductionCompany> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ProductionCompany> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public List<ProductionCountry> getProductionCountries() {
        return productionCountries;
    }

    public void setProductionCountries(List<ProductionCountry> productionCountries) {
        this.productionCountries = productionCountries;
    }

    public List<SpokenLanguage> getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(List<SpokenLanguage> spokenLanguages) {
        this.spokenLanguages = spokenLanguages;
    }

    // Helper methods
    public String getYear() {
        if (releaseDate != null && !releaseDate.isEmpty()) {
            return releaseDate.substring(0, 4);
        }
        return "";
    }

    public String getFormattedRuntime() {
        if (runtime <= 0) return "";
        int hours = runtime / 60;
        int minutes = runtime % 60;
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        } else {
            return minutes + "m";
        }
    }

    public String getFormattedRating() {
        return String.format("%.1f", voteAverage);
    }

    // Inner classes for nested objects
    public static class Genre {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;

        public Genre() {}

        public Genre(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class ProductionCompany {
        @SerializedName("id")
        private int id;
        
        @SerializedName("name")
        private String name;
        
        @SerializedName("logo_path")
        private String logoPath;
        
        @SerializedName("origin_country")
        private String originCountry;

        public ProductionCompany() {}

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLogoPath() {
            return logoPath;
        }

        public void setLogoPath(String logoPath) {
            this.logoPath = logoPath;
        }

        public String getOriginCountry() {
            return originCountry;
        }

        public void setOriginCountry(String originCountry) {
            this.originCountry = originCountry;
        }
    }

    public static class ProductionCountry {
        @SerializedName("iso_3166_1")
        private String iso31661;
        
        @SerializedName("name")
        private String name;

        public ProductionCountry() {}

        public String getIso31661() {
            return iso31661;
        }

        public void setIso31661(String iso31661) {
            this.iso31661 = iso31661;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class SpokenLanguage {
        @SerializedName("iso_639_1")
        private String iso6391;
        
        @SerializedName("name")
        private String name;

        public SpokenLanguage() {}

        public String getIso6391() {
            return iso6391;
        }

        public void setIso6391(String iso6391) {
            this.iso6391 = iso6391;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}