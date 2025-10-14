package com.example.popflix;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.popflix.ApiClient;
import com.example.popflix.ApiService;
import com.example.popflix.Movie;
import com.example.popflix.MovieResponse;
import com.example.popflix.Review;
import com.example.popflix.ReviewAdapter;
import com.example.popflix.Video;
import com.example.popflix.VideoResponse;
import com.example.popflix.databinding.ActivityDetailsBinding;
import com.example.popflix.utils.WatchlistManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {
    
    private ActivityDetailsBinding binding;
    private Movie movie;
    private ApiService apiService;
    private WatchlistManager watchlistManager;
    private boolean isInWatchlist = false;
    private boolean isInFavorites = false;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        apiService = ApiClient.getClient(this);

        // Initialize WatchlistManager
        watchlistManager = new WatchlistManager();

        // Initialize Firebase components
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Get movie data from intent
        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            Toast.makeText(this, "Movie data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupWatchlistButton();
        setupFavoritesButton();
        loadMovieDetails();
        checkWatchlistStatus();
        checkFavoritesStatus();
        setupReviews();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
    }

    private void setupWatchlistButton() {
        binding.addToWatchlistButton.setOnClickListener(v -> toggleWatchlist());
    }

    private void setupFavoritesButton() {
        binding.addToFavoritesButton.setOnClickListener(v -> toggleFavorites());
    }

    private void checkFavoritesStatus() {
        if (currentUser == null) {
            return;
        }
        String userId = currentUser.getUid();

        firestore.collection("favorites")
                .whereEqualTo("userId", userId)
                .whereEqualTo("movieId", movie.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        isInFavorites = !queryDocumentSnapshots.isEmpty();
                        updateFavoritesButton();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DetailsActivity.this, "Error checking favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void toggleFavorites() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to use favorites features", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = currentUser.getUid();

        if (isInFavorites) {
            // Remove from favorites
            firestore.collection("favorites")
                    .whereEqualTo("userId", userId)
                    .whereEqualTo("movieId", movie.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                                doc.getReference().delete();
                            }
                            isInFavorites = false;
                            updateFavoritesButton();
                            Toast.makeText(DetailsActivity.this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DetailsActivity.this, "Error removing from favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            // Add to favorites
            Map<String, Object> favoriteData = new HashMap<>();
            favoriteData.put("userId", userId);
            favoriteData.put("movieId", movie.getId());
            favoriteData.put("movieTitle", movie.getTitle());
            favoriteData.put("moviePoster", movie.getPosterPath());
            favoriteData.put("timestamp", System.currentTimeMillis());

            firestore.collection("favorites")
                    .add(favoriteData)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            isInFavorites = true;
                            updateFavoritesButton();
                            Toast.makeText(DetailsActivity.this, "Added to favorites", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(DetailsActivity.this, "Error adding to favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void updateFavoritesButton() {
        if (isInFavorites) {
            binding.addToFavoritesButton.setIconResource(R.drawable.ic_heart_filled);
            binding.addToFavoritesButton.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brand_red)));
        } else {
            binding.addToFavoritesButton.setIconResource(R.drawable.ic_heart_outline);
            binding.addToFavoritesButton.setIconTint(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_secondary)));
        }
    }

    private void setupReviews() {
        reviewList = new ArrayList<>();
        reviewAdapter = new ReviewAdapter(reviewList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.reviewsRecyclerView.setLayoutManager(layoutManager);
        binding.reviewsRecyclerView.setAdapter(reviewAdapter);

        // Load existing reviews
        loadReviews();

        // Setup add review button
        binding.addReviewButton.setOnClickListener(v -> showAddReviewDialog());
    }

    private void loadReviews() {
        firestore.collection("reviews")
                .whereEqualTo("movieId", movie.getId())
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        reviewList.clear();
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            review.setId(doc.getId());
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();

                        // Update reviews count
                        binding.reviewsCount.setText(reviewList.size() + " reviews");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DetailsActivity.this, "Error loading reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showAddReviewDialog() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to add reviews", Toast.LENGTH_SHORT).show();
            return;
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Add Review");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1.0f);
        ratingBar.setRating(5.0f);
        layout.addView(ratingBar);

        TextInputEditText reviewText = new TextInputEditText(this);
        reviewText.setHint("Write your review...");
        reviewText.setMaxLines(4);
        layout.addView(reviewText);

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = ratingBar.getRating();
            String reviewContent = reviewText.getText() != null ? reviewText.getText().toString().trim() : "";

            if (reviewContent.isEmpty()) {
                Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
                return;
            }

            addReview(rating, reviewContent);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addReview(float rating, String content) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", currentUser.getUid());
        reviewData.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
        reviewData.put("movieId", movie.getId());
        reviewData.put("movieTitle", movie.getTitle());
        reviewData.put("rating", rating);
        reviewData.put("content", content);
        reviewData.put("timestamp", System.currentTimeMillis());

        firestore.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(DetailsActivity.this, "Review added successfully", Toast.LENGTH_SHORT).show();
                        loadReviews(); // Reload reviews
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DetailsActivity.this, "Error adding review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMovieDetails() {
        // Set movie title
        if (movie.getTitle() != null) {
            binding.movieTitle.setText(movie.getTitle());
        }

        // Set movie year, runtime, and rating
        String year = movie.getYear();
        String runtime = movie.getFormattedRuntime();
        String rating = movie.getFormattedRating();

        StringBuilder details = new StringBuilder();
        if (!year.isEmpty()) {
            details.append(year);
        }
        if (!runtime.isEmpty()) {
            if (details.length() > 0) details.append(" • ");
            details.append(runtime);
        }
        if (!rating.isEmpty()) {
            if (details.length() > 0) details.append(" • ");
            details.append(rating);
        }

        binding.movieYear.setText(details.toString());

        // Load backdrop image
        if (movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty()) {
            String backdropUrl = "https://image.tmdb.org/t/p/w1280" + movie.getBackdropPath();
            Glide.with(this)
                    .load(backdropUrl)
                    .into(binding.backdropImage);
        }

        // Set genres
        setupGenres();

        // Set plot
        if (movie.getOverview() != null) {
            binding.moviePlot.setText(movie.getOverview());
        }

        // Setup trailer button
        binding.watchTrailerButton.setOnClickListener(v -> openTrailer());
    }

    private void setupGenres() {
        binding.genreChips.removeAllViews();

        if (movie.getGenres() != null && !movie.getGenres().isEmpty()) {
            for (Movie.Genre genre : movie.getGenres()) {
                Chip chip = new Chip(this);
                chip.setText(genre.getName());
                chip.setChipBackgroundColorResource(R.color.chip_background);
                chip.setTextColor(getResources().getColor(R.color.text_primary));
                binding.genreChips.addView(chip);
            }
        }
    }

    private void openTrailer() {
        // Show loading indicator
        binding.watchTrailerButton.setEnabled(false);
        binding.watchTrailerButton.setText("Loading...");

        Call<VideoResponse> call = apiService.getMovieVideos(movie.getId());
        call.enqueue(new retrofit2.Callback<VideoResponse>() {
            @Override
            public void onResponse(Call<VideoResponse> call, retrofit2.Response<VideoResponse> response) {
                binding.watchTrailerButton.setEnabled(true);
                binding.watchTrailerButton.setText(getString(R.string.watch_trailer));

                if (response.isSuccessful() && response.body() != null) {
                    VideoResponse videoResponse = response.body();
                    List<Video> videos = videoResponse.getResults();

                    // Find the best trailer
                    Video bestTrailer = findBestTrailer(videos);

                    if (bestTrailer != null) {
                        String youtubeUrl = bestTrailer.getYouTubeUrl();
                        if (youtubeUrl != null) {
                            openYouTubeTrailer(youtubeUrl);
                        } else {
                            fallbackToHomepage();
                        }
                    } else {
                        fallbackToHomepage();
                    }
                } else {
                    fallbackToHomepage();
                }
            }

            @Override
            public void onFailure(Call<VideoResponse> call, Throwable t) {
                binding.watchTrailerButton.setEnabled(true);
                binding.watchTrailerButton.setText(getString(R.string.watch_trailer));
                fallbackToHomepage();
            }
        });
    }

    private Video findBestTrailer(List<Video> videos) {
        if (videos == null || videos.isEmpty()) {
            return null;
        }

        // First, try to find an official trailer
        for (Video video : videos) {
            if (video.isTrailer() && video.isOfficial()) {
                return video;
            }
        }

        // If no official trailer, find any trailer
        for (Video video : videos) {
            if (video.isTrailer()) {
                return video;
            }
        }

        // If no trailers, find any video
        for (Video video : videos) {
            if ("YouTube".equalsIgnoreCase(video.getSite())) {
                return video;
            }
        }

        return null;
    }

    private void openYouTubeTrailer(String youtubeUrl) {
        try {
            Intent intent = new Intent(this, TrailerActivity.class);
            intent.putExtra("videoUrl", youtubeUrl);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Unable to open trailer", Toast.LENGTH_SHORT).show();
            fallbackToHomepage();
        }
    }

    private void fallbackToHomepage() {
        // For now, we'll open the movie's homepage or IMDB page
        String url = movie.getHomepage();
        if (url == null || url.isEmpty()) {
            if (movie.getImdbId() != null && !movie.getImdbId().isEmpty()) {
                url = "https://www.imdb.com/title/" + movie.getImdbId();
            } else {
                Toast.makeText(this, "Trailer not available", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void checkWatchlistStatus() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to use watchlist features", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        watchlistManager.isMovieInWatchlist(userId, movie.getId(), new WatchlistManager.WatchlistCallback() {
            @Override
            public void onSuccess(boolean inWatchlist) {
                isInWatchlist = inWatchlist;
                updateWatchlistButton();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(DetailsActivity.this, "Error checking watchlist: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleWatchlist() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to use watchlist features", Toast.LENGTH_SHORT).show();
            return;
        }
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        if (isInWatchlist) {
            // Remove from watchlist
            watchlistManager.removeFromWatchlist(userId, movie.getId(), new WatchlistManager.WatchlistCallback() {
                @Override
                public void onSuccess(boolean result) {
                    isInWatchlist = false;
                    updateWatchlistButton();
                    Toast.makeText(DetailsActivity.this, "Removed from watchlist", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(DetailsActivity.this, "Error removing from watchlist: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Add to watchlist
            watchlistManager.addToWatchlist(userId, movie, new WatchlistManager.WatchlistCallback() {
                @Override
                public void onSuccess(boolean result) {
                    isInWatchlist = true;
                    updateWatchlistButton();
                    Toast.makeText(DetailsActivity.this, "Added to watchlist", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(DetailsActivity.this, "Error adding to watchlist: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void updateWatchlistButton() {
        if (isInWatchlist) {
            binding.addToWatchlistButton.setText("Remove from Watchlist");
            binding.addToWatchlistButton.setIconResource(R.drawable.ic_check);
        } else {
            binding.addToWatchlistButton.setText("Add to Watchlist");
            binding.addToWatchlistButton.setIconResource(R.drawable.ic_add);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
