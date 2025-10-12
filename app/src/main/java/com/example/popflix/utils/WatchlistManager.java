package com.example.popflix.utils;

import com.example.popflix.Movie;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchlistManager {
    
    private static final String WATCHLIST_COLLECTION = "watchlists";
    private static final String MOVIES_FIELD = "movies";
    private static final String ADDED_AT_FIELD = "addedAt";
    
    private FirebaseFirestore db;
    
    public WatchlistManager() {
        this.db = FirebaseFirestore.getInstance();
    }
    
    public interface WatchlistCallback {
        void onSuccess(boolean result);
        void onError(String error);
    }
    
    public interface WatchlistMoviesCallback {
        void onSuccess(List<Movie> movies);
        void onError(String error);
    }
    
    /**
     * Add a movie to the user's watchlist
     */
    public void addToWatchlist(String userId, Movie movie, WatchlistCallback callback) {
        DocumentReference watchlistRef = db.collection(WATCHLIST_COLLECTION).document(userId);
        
        // First check if the movie is already in the watchlist
        watchlistRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // Watchlist exists, check if movie is already there
                    List<Map<String, Object>> movies = (List<Map<String, Object>>) document.get(MOVIES_FIELD);
                    if (movies != null) {
                        for (Map<String, Object> movieData : movies) {
                            if (movieData.get("id").equals(movie.getId())) {
                                callback.onError("Movie already in watchlist");
                                return;
                            }
                        }
                    }
                    
                    // Add movie to existing watchlist
                    addMovieToExistingWatchlist(watchlistRef, movie, callback);
                } else {
                    // Create new watchlist with this movie
                    createNewWatchlist(watchlistRef, movie, callback);
                }
            } else {
                callback.onError("Error checking watchlist: " + task.getException().getMessage());
            }
        });
    }
    
    /**
     * Remove a movie from the user's watchlist
     */
    public void removeFromWatchlist(String userId, int movieId, WatchlistCallback callback) {
        DocumentReference watchlistRef = db.collection(WATCHLIST_COLLECTION).document(userId);
        
        watchlistRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Object>> movies = (List<Map<String, Object>>) document.get(MOVIES_FIELD);
                    if (movies != null) {
                        // Remove the movie with the given ID
                        movies.removeIf(movieData -> movieData.get("id").equals(movieId));
                        
                        // Update the watchlist
                        Map<String, Object> updates = new HashMap<>();
                        updates.put(MOVIES_FIELD, movies);
                        
                        watchlistRef.update(updates)
                                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                                .addOnFailureListener(e -> callback.onError("Error removing movie: " + e.getMessage()));
                    } else {
                        callback.onError("No movies found in watchlist");
                    }
                } else {
                    callback.onError("Watchlist not found");
                }
            } else {
                callback.onError("Error accessing watchlist: " + task.getException().getMessage());
            }
        });
    }
    
    /**
     * Check if a movie is in the user's watchlist
     */
    public void isMovieInWatchlist(String userId, int movieId, WatchlistCallback callback) {
        DocumentReference watchlistRef = db.collection(WATCHLIST_COLLECTION).document(userId);
        
        watchlistRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Object>> movies = (List<Map<String, Object>>) document.get(MOVIES_FIELD);
                    if (movies != null) {
                        boolean found = false;
                        for (Map<String, Object> movieData : movies) {
                            if (movieData.get("id").equals(movieId)) {
                                found = true;
                                break;
                            }
                        }
                        callback.onSuccess(found);
                    } else {
                        callback.onSuccess(false);
                    }
                } else {
                    callback.onSuccess(false);
                }
            } else {
                callback.onError("Error checking watchlist: " + task.getException().getMessage());
            }
        });
    }
    
    /**
     * Get all movies from the user's watchlist
     */
    public void getWatchlistMovies(String userId, WatchlistMoviesCallback callback) {
        DocumentReference watchlistRef = db.collection(WATCHLIST_COLLECTION).document(userId);
        
        watchlistRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Object>> moviesData = (List<Map<String, Object>>) document.get(MOVIES_FIELD);
                    if (moviesData != null && !moviesData.isEmpty()) {
                        List<Movie> movies = new ArrayList<>();
                        for (Map<String, Object> movieData : moviesData) {
                            Movie movie = convertMapToMovie(movieData);
                            if (movie != null) {
                                movies.add(movie);
                            }
                        }
                        callback.onSuccess(movies);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            } else {
                callback.onError("Error getting watchlist: " + task.getException().getMessage());
            }
        });
    }
    
    /**
     * Get watchlist count
     */
    public void getWatchlistCount(String userId, WatchlistCallback callback) {
        DocumentReference watchlistRef = db.collection(WATCHLIST_COLLECTION).document(userId);
        
        watchlistRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Map<String, Object>> movies = (List<Map<String, Object>>) document.get(MOVIES_FIELD);
                    int count = movies != null ? movies.size() : 0;
                    callback.onSuccess(count > 0);
                } else {
                    callback.onSuccess(false);
                }
            } else {
                callback.onError("Error getting watchlist count: " + task.getException().getMessage());
            }
        });
    }
    
    private void createNewWatchlist(DocumentReference watchlistRef, Movie movie, WatchlistCallback callback) {
        Map<String, Object> watchlist = new HashMap<>();
        List<Map<String, Object>> movies = new ArrayList<>();
        movies.add(convertMovieToMap(movie));
        watchlist.put(MOVIES_FIELD, movies);
        watchlist.put("createdAt", System.currentTimeMillis());
        
        watchlistRef.set(watchlist)
                .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                .addOnFailureListener(e -> callback.onError("Error creating watchlist: " + e.getMessage()));
    }
    
    private void addMovieToExistingWatchlist(DocumentReference watchlistRef, Movie movie, WatchlistCallback callback) {
        watchlistRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                List<Map<String, Object>> movies = (List<Map<String, Object>>) document.get(MOVIES_FIELD);
                if (movies == null) {
                    movies = new ArrayList<>();
                }
                
                movies.add(convertMovieToMap(movie));
                
                Map<String, Object> updates = new HashMap<>();
                updates.put(MOVIES_FIELD, movies);
                
                watchlistRef.update(updates)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(true))
                        .addOnFailureListener(e -> callback.onError("Error adding movie: " + e.getMessage()));
            } else {
                callback.onError("Error accessing watchlist: " + task.getException().getMessage());
            }
        });
    }
    
    private Map<String, Object> convertMovieToMap(Movie movie) {
        Map<String, Object> movieMap = new HashMap<>();
        movieMap.put("id", movie.getId());
        movieMap.put("title", movie.getTitle());
        movieMap.put("overview", movie.getOverview());
        movieMap.put("posterPath", movie.getPosterPath());
        movieMap.put("backdropPath", movie.getBackdropPath());
        movieMap.put("releaseDate", movie.getReleaseDate());
        movieMap.put("voteAverage", movie.getVoteAverage());
        movieMap.put("voteCount", movie.getVoteCount());
        movieMap.put("popularity", movie.getPopularity());
        movieMap.put("adult", movie.isAdult());
        movieMap.put("video", movie.isVideo());
        movieMap.put("originalLanguage", movie.getOriginalLanguage());
        movieMap.put("originalTitle", movie.getOriginalTitle());
        movieMap.put("genreIds", movie.getGenreIds());
        movieMap.put("runtime", movie.getRuntime());
        movieMap.put("status", movie.getStatus());
        movieMap.put("tagline", movie.getTagline());
        movieMap.put("budget", movie.getBudget());
        movieMap.put("revenue", movie.getRevenue());
        movieMap.put("homepage", movie.getHomepage());
        movieMap.put("imdbId", movie.getImdbId());
        movieMap.put(ADDED_AT_FIELD, System.currentTimeMillis());
        
        return movieMap;
    }
    
    private Movie convertMapToMovie(Map<String, Object> movieMap) {
        try {
            Movie movie = new Movie();
            movie.setId(((Number) movieMap.get("id")).intValue());
            movie.setTitle((String) movieMap.get("title"));
            movie.setOverview((String) movieMap.get("overview"));
            movie.setPosterPath((String) movieMap.get("posterPath"));
            movie.setBackdropPath((String) movieMap.get("backdropPath"));
            movie.setReleaseDate((String) movieMap.get("releaseDate"));
            
            Object voteAverage = movieMap.get("voteAverage");
            if (voteAverage instanceof Number) {
                movie.setVoteAverage(((Number) voteAverage).doubleValue());
            }
            
            Object voteCount = movieMap.get("voteCount");
            if (voteCount instanceof Number) {
                movie.setVoteCount(((Number) voteCount).intValue());
            }
            
            Object popularity = movieMap.get("popularity");
            if (popularity instanceof Number) {
                movie.setPopularity(((Number) popularity).doubleValue());
            }
            
            movie.setAdult(Boolean.TRUE.equals(movieMap.get("adult")));
            movie.setVideo(Boolean.TRUE.equals(movieMap.get("video")));
            movie.setOriginalLanguage((String) movieMap.get("originalLanguage"));
            movie.setOriginalTitle((String) movieMap.get("originalTitle"));
            movie.setGenreIds((List<Integer>) movieMap.get("genreIds"));
            
            Object runtime = movieMap.get("runtime");
            if (runtime instanceof Number) {
                movie.setRuntime(((Number) runtime).intValue());
            }
            
            movie.setStatus((String) movieMap.get("status"));
            movie.setTagline((String) movieMap.get("tagline"));
            
            Object budget = movieMap.get("budget");
            if (budget instanceof Number) {
                movie.setBudget(((Number) budget).longValue());
            }
            
            Object revenue = movieMap.get("revenue");
            if (revenue instanceof Number) {
                movie.setRevenue(((Number) revenue).longValue());
            }
            
            movie.setHomepage((String) movieMap.get("homepage"));
            movie.setImdbId((String) movieMap.get("imdbId"));
            
            return movie;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
