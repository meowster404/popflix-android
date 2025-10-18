package com.example.popflix;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
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
import com.example.popflix.ReviewResponse;
import com.example.popflix.Video;
import com.example.popflix.VideoResponse;
import com.example.popflix.databinding.ActivityDetailsBinding;
import com.example.popflix.utils.WatchlistManager;
import com.example.popflix.utils.ThemeManager;
import com.bumptech.glide.Glide;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnFailureListener;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private Movie movie;
    private ApiService apiService;
    private WatchlistManager watchlistManager;
    private boolean isInWatchlist = false;
    private boolean isInFavorites = false;
    private int selectedRating = 0; // Default to no rating selected
    private ReviewAdapter reviewAdapter;
    private List<Review> reviewList;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize and apply theme before calling super.onCreate
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        apiService = ApiClient.getClient();

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
            favoriteData.put("movieOverview", movie.getOverview());
            favoriteData.put("movieReleaseDate", movie.getReleaseDate());
            favoriteData.put("movieBackdrop", movie.getBackdropPath());
            favoriteData.put("movieVoteAverage", movie.getVoteAverage());
            favoriteData.put("movieVoteCount", movie.getVoteCount());
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
    }

    private void loadReviews() {
        reviewList.clear();
        
        // Load Firebase reviews first
        firestore.collection("reviews")
                .whereEqualTo("movieId", movie.getId())
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Review review = doc.toObject(Review.class);
                            review.setId(doc.getId());
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                        
                        // After loading Firebase reviews, load TMDB reviews
                        loadTMDBReviews();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(DetailsActivity.this, "Error loading reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        // Still try to load TMDB reviews even if Firebase fails
                        loadTMDBReviews();
                    }
                });
    }
    
    private void loadTMDBReviews() {
        Call<ReviewResponse> call = apiService.getMovieReviews(movie.getId());
        call.enqueue(new Callback<ReviewResponse>() {
            @Override
            public void onResponse(Call<ReviewResponse> call, Response<ReviewResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ReviewResponse reviewResponse = response.body();
                    if (reviewResponse.getResults() != null) {
                        for (ReviewResponse.TMDBReview tmdbReview : reviewResponse.getResults()) {
                            // Convert TMDB review to our Review format
                            Review review = new Review();
                            review.setId(tmdbReview.getId());
                            review.setUserName(tmdbReview.getAuthor());
                            review.setContent(tmdbReview.getContent());
                            review.setTimestamp(System.currentTimeMillis()); // Use current time as fallback
                            
                            // Set rating from author details if available
                            if (tmdbReview.getAuthorDetails() != null && tmdbReview.getAuthorDetails().getRating() != null) {
                                // Convert 10-point scale to 5-point scale
                                int rating = (int) Math.ceil(tmdbReview.getAuthorDetails().getRating() / 2.0);
                                review.setRating(Math.max(1, Math.min(5, rating)));
                            } else {
                                review.setRating(0); // No rating available
                            }
                            
                            // Mark as TMDB review
                            review.setUserId("tmdb_" + tmdbReview.getId());
                            
                            reviewList.add(review);
                        }
                        reviewAdapter.notifyDataSetChanged();
                    }
                }
            }
            
            @Override
            public void onFailure(Call<ReviewResponse> call, Throwable t) {
                // Silently fail for TMDB reviews - not critical
                Log.e("DetailsActivity", "Failed to load TMDB reviews: " + t.getMessage());
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

        // Rating buttons for dialog
        LinearLayout ratingContainer = new LinearLayout(this);
        ratingContainer.setOrientation(LinearLayout.HORIZONTAL);
        ratingContainer.setGravity(Gravity.CENTER);

        final int[] dialogSelectedRating = {0}; // Default to no rating

        // Add rating buttons to dialog
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            MaterialButton ratingBtn = new MaterialButton(this);
            ratingBtn.setText(String.valueOf(i));
            ratingBtn.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
            ratingBtn.setCornerRadius(24);
            ratingBtn.setTextSize(14);
            ratingBtn.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            // Set initial state based on dialog selection
            if (rating <= dialogSelectedRating[0]) {
                ratingBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_selected));
                ratingBtn.setTextColor(Color.WHITE);
            } else {
                ratingBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_unselected));
                ratingBtn.setTextColor(getColor(R.color.text_primary));
            }

            ratingBtn.setOnClickListener(v -> {
                dialogSelectedRating[0] = rating;
                // Update all button states in dialog
                for (int j = 0; j < ratingContainer.getChildCount(); j++) {
                    MaterialButton btn = (MaterialButton) ratingContainer.getChildAt(j);
                    int btnRating = j + 1;
                    if (btnRating <= dialogSelectedRating[0]) {
                        btn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_selected));
                        btn.setTextColor(Color.WHITE);
                    } else {
                        btn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_unselected));
                        btn.setTextColor(getColor(R.color.text_primary));
                    }
                }
            });

            ratingContainer.addView(ratingBtn);
        }

        layout.addView(ratingContainer);

        TextInputEditText reviewText = new TextInputEditText(this);
        reviewText.setHint("Write your review...");
        reviewText.setMaxLines(4);
        layout.addView(reviewText);

        builder.setView(layout);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String reviewContent = reviewText.getText() != null ? reviewText.getText().toString().trim() : "";

            if (reviewContent.isEmpty()) {
                Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
                return;
            }

            addReview(dialogSelectedRating[0], reviewContent);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void addReview(int rating, String content) {
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

    private void loadMovieDetails() {
        // Set movie title
        if (movie.getTitle() != null) {
            binding.movieTitle.setText(movie.getTitle());
        } else {
            binding.movieTitle.setText("Unknown Title");
        }

        // Set movie year only
        String year = movie.getYear();
        if (year != null && !year.isEmpty()) {
            binding.movieYear.setText(year);
            binding.movieYear.setVisibility(View.VISIBLE);
        } else {
            binding.movieYear.setVisibility(View.GONE);
        }

        // Set movie runtime only
        String runtime = movie.getFormattedRuntime();
        if (runtime != null && !runtime.isEmpty()) {
            binding.movieRuntime.setText(runtime);
            binding.movieRuntime.setVisibility(View.VISIBLE);
        } else {
            binding.movieRuntime.setVisibility(View.GONE);
        }

        // Set movie rating only (remove duplicate)
        if (movie.getVoteAverage() > 0) {
            binding.movieRating.setText(String.format("%.1f/10", movie.getVoteAverage()));
            binding.movieRating.setVisibility(View.VISIBLE);
        } else {
            binding.movieRating.setVisibility(View.GONE);
        }

        // Apply theme-aware colors to poster background
        ThemeManager themeManager = ThemeManager.getInstance(this);
        binding.moviePoster.setBackgroundColor(themeManager.getCardBackgroundColor(this));

        // Load main movie poster
        if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
            String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            Glide.with(this)
                    .load(posterUrl)
                    .into(binding.moviePoster);
        }

        // Load background poster
        if (movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty()) {
            String backdropUrl = "https://image.tmdb.org/t/p/w1280" + movie.getBackdropPath();
            Glide.with(this)
                    .load(backdropUrl)
                    .into(binding.backgroundPoster);
            binding.backgroundPoster.setVisibility(View.VISIBLE);
            binding.blurOverlay.setVisibility(View.VISIBLE);
            binding.shadeOverlay.setVisibility(View.VISIBLE);
        }

        // Set plot with expand/collapse functionality
        if (movie.getOverview() != null && !movie.getOverview().isEmpty()) {
            binding.moviePlot.setText(movie.getOverview());
            setupExpandablePlot();
        } else {
            binding.moviePlot.setText("No plot summary available.");
            binding.expandPlotButton.setVisibility(View.GONE);
        }

        // Setup buttons
        binding.addToFavoritesButton.setOnClickListener(v -> toggleFavorites());

        // Setup review system
        setupReviewSystem();
        
        // Setup genres
        setupGenres();
    }

    private void setupExpandablePlot() {
        if (movie.getOverview() != null && movie.getOverview().length() > 300) {
            // Show expand/collapse button only for long plots
            binding.expandPlotButton.setVisibility(View.VISIBLE);
            binding.expandPlotButton.setOnClickListener(v -> {
                if (binding.moviePlot.getMaxLines() == 3) {
                    // Expand plot
                    binding.moviePlot.setMaxLines(Integer.MAX_VALUE);
                    binding.expandPlotButton.setText("Show Less");
                    binding.expandPlotButton.setIconResource(R.drawable.ic_expand_less);
                } else {
                    // Collapse plot
                    binding.moviePlot.setMaxLines(3);
                    binding.expandPlotButton.setText("Show More");
                    binding.expandPlotButton.setIconResource(R.drawable.ic_expand_more);
                }
            });
        } else {
            // Hide expand button for short plots
            binding.expandPlotButton.setVisibility(View.GONE);
        }
    }

    private void setupReviewSystem() {
        // Setup rating buttons
        setupRatingButtons();

        // Setup submit review button
        binding.submitReviewButton.setOnClickListener(v -> submitReview());

        // Check if user already has a review for this movie
        checkUserReview();
    }

    private void setupRatingButtons() {
        // Rating button 1
        MaterialButton btn1 = findViewById(R.id.rating_btn_1);
        if (btn1 != null) {
            btn1.setOnClickListener(v -> selectRating(1));
        }

        // Rating button 2
        MaterialButton btn2 = findViewById(R.id.rating_btn_2);
        if (btn2 != null) {
            btn2.setOnClickListener(v -> selectRating(2));
        }

        // Rating button 3
        MaterialButton btn3 = findViewById(R.id.rating_btn_3);
        if (btn3 != null) {
            btn3.setOnClickListener(v -> selectRating(3));
        }

        // Rating button 4
        MaterialButton btn4 = findViewById(R.id.rating_btn_4);
        if (btn4 != null) {
            btn4.setOnClickListener(v -> selectRating(4));
        }

        // Rating button 5
        MaterialButton btn5 = findViewById(R.id.rating_btn_5);
        if (btn5 != null) {
            btn5.setOnClickListener(v -> selectRating(5));
        }

        // Set default selection (no rating selected - all buttons transparent)
        selectRating(0);
    }

    private void selectRating(int rating) {
        selectedRating = rating;

        // Update button visual states
        updateRatingButtonStates();
    }

    private void updateRatingButtonStates() {
        // Button 1
        MaterialButton btn1 = findViewById(R.id.rating_btn_1);
        if (btn1 != null) {
            updateButtonState(btn1, 1);
        }

        // Button 2
        MaterialButton btn2 = findViewById(R.id.rating_btn_2);
        if (btn2 != null) {
            updateButtonState(btn2, 2);
        }

        // Button 3
        MaterialButton btn3 = findViewById(R.id.rating_btn_3);
        if (btn3 != null) {
            updateButtonState(btn3, 3);
        }

        // Button 4
        MaterialButton btn4 = findViewById(R.id.rating_btn_4);
        if (btn4 != null) {
            updateButtonState(btn4, 4);
        }

        // Button 5
        MaterialButton btn5 = findViewById(R.id.rating_btn_5);
        if (btn5 != null) {
            updateButtonState(btn5, 5);
        }
    }

    private void updateButtonState(MaterialButton button, int buttonRating) {
        if (buttonRating <= selectedRating) {
            // Selected state - red background with white text
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brand_red)));
            button.setTextColor(Color.WHITE);
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brand_red)));
        } else {
            // Unselected state - transparent background with visible text
            button.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
            button.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.glass_stroke)));
        }
        
        // Ensure text is always visible by setting a contrasting color
        button.setTextSize(16f);
        button.setTypeface(Typeface.DEFAULT_BOLD);
    }

    private void checkUserReview() {
        if (currentUser == null) {
            return;
        }

        firestore.collection("reviews")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("movieId", movie.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // User already has a review, show edit/delete options
                        binding.addReviewSection.setVisibility(View.GONE);
                        setupExistingReviewOptions();
                    } else {
                        // User can add a new review
                        binding.addReviewSection.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error checking reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setupExistingReviewOptions() {
        // Create edit/delete buttons for existing review
        LinearLayout existingReviewLayout = new LinearLayout(this);
        existingReviewLayout.setOrientation(LinearLayout.HORIZONTAL);
        existingReviewLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));

        MaterialButton editButton = new MaterialButton(this);
        editButton.setText("Edit Review");
        editButton.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        editButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary_purple)));
        editButton.setTextColor(getColor(R.color.white));
        editButton.setCornerRadius(24);

        MaterialButton deleteButton = new MaterialButton(this);
        deleteButton.setText("Delete Review");
        deleteButton.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        deleteButton.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.brand_red)));
        deleteButton.setTextColor(getColor(R.color.white));
        deleteButton.setCornerRadius(24);

        existingReviewLayout.addView(editButton);
        existingReviewLayout.addView(deleteButton);

        // Add to reviews section
        LinearLayout reviewsContainer = findViewById(R.id.reviews_section);
        if (reviewsContainer != null) {
            reviewsContainer.addView(existingReviewLayout, 1); // Add after title
        }

        editButton.setOnClickListener(v -> editReview());
        deleteButton.setOnClickListener(v -> deleteReview());
    }

    private void submitReview() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to submit reviews", Toast.LENGTH_SHORT).show();
            return;
        }

        String reviewText = binding.reviewTextInput.getText() != null ?
                binding.reviewTextInput.getText().toString().trim() : "";

        if (reviewText.isEmpty()) {
            Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedRating == 0) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add review to Firestore
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("userId", currentUser.getUid());
        reviewData.put("userName", currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Anonymous");
        reviewData.put("movieId", movie.getId());
        reviewData.put("movieTitle", movie.getTitle());
        reviewData.put("rating", selectedRating);
        reviewData.put("content", reviewText);
        reviewData.put("timestamp", System.currentTimeMillis());

        firestore.collection("reviews")
                .add(reviewData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Review submitted successfully", Toast.LENGTH_SHORT).show();
                    binding.reviewTextInput.setText("");
                    // Reset rating buttons to no selection
                    selectRating(0);
                    checkUserReview(); // Refresh UI
                })
                .addOnFailureListener(e -> {
                    Log.e("DetailsActivity", "Error submitting review: " + e.getMessage(), e);
                    Toast.makeText(this, "Error submitting review: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void editReview() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to edit reviews", Toast.LENGTH_SHORT).show();
            return;
        }

        // Find the user's existing review for this movie
        firestore.collection("reviews")
                .whereEqualTo("userId", currentUser.getUid())
                .whereEqualTo("movieId", movie.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Get the first (and should be only) review
                        QueryDocumentSnapshot doc = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);
                        Review existingReview = doc.toObject(Review.class);
                        existingReview.setId(doc.getId());

                        showEditReviewDialog(existingReview);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading review for editing: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void showEditReviewDialog(Review existingReview) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Edit Review");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32, 16, 32, 16);

        // Rating buttons for dialog
        LinearLayout ratingContainer = new LinearLayout(this);
        ratingContainer.setOrientation(LinearLayout.HORIZONTAL);
        ratingContainer.setGravity(Gravity.CENTER);

        final int[] dialogSelectedRating = {(int) existingReview.getRating()}; // Default to existing rating

        // Add rating buttons to dialog
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            MaterialButton ratingBtn = new MaterialButton(this);
            ratingBtn.setText(String.valueOf(i));
            ratingBtn.setLayoutParams(new LinearLayout.LayoutParams(48, 48));
            ratingBtn.setCornerRadius(24);
            ratingBtn.setTextSize(14);
            ratingBtn.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            // Set initial state based on existing rating
            if (rating <= dialogSelectedRating[0]) {
                ratingBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_selected));
                ratingBtn.setTextColor(Color.WHITE);
            } else {
                ratingBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_unselected));
                ratingBtn.setTextColor(getColor(R.color.text_primary));
            }

            ratingBtn.setOnClickListener(v -> {
                dialogSelectedRating[0] = rating;
                // Update all button states in dialog
                for (int j = 0; j < ratingContainer.getChildCount(); j++) {
                    MaterialButton btn = (MaterialButton) ratingContainer.getChildAt(j);
                    int btnRating = j + 1;
                    if (btnRating <= dialogSelectedRating[0]) {
                        btn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_selected));
                        btn.setTextColor(Color.WHITE);
                    } else {
                        btn.setBackground(ContextCompat.getDrawable(this, R.drawable.glass_button_unselected));
                        btn.setTextColor(getColor(R.color.text_primary));
                    }
                }
            });

            ratingContainer.addView(ratingBtn);
        }

        layout.addView(ratingContainer);

        TextInputEditText reviewText = new TextInputEditText(this);
        reviewText.setHint("Write your review...");
        reviewText.setMaxLines(4);
        reviewText.setText(existingReview.getContent()); // Pre-populate with existing content
        layout.addView(reviewText);

        builder.setView(layout);

        builder.setPositiveButton("Update", (dialog, which) -> {
            String reviewContent = reviewText.getText() != null ? reviewText.getText().toString().trim() : "";

            if (reviewContent.isEmpty()) {
                Toast.makeText(this, "Please write a review", Toast.LENGTH_SHORT).show();
                return;
            }

            updateReview(existingReview.getId(), dialogSelectedRating[0], reviewContent);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateReview(String reviewId, int rating, String content) {
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("rating", rating);
        reviewData.put("content", content);
        reviewData.put("timestamp", System.currentTimeMillis());

        firestore.collection("reviews").document(reviewId)
                .update(reviewData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Review updated successfully", Toast.LENGTH_SHORT).show();
                    loadReviews(); // Reload reviews
                    checkUserReview(); // Refresh UI
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error updating review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteReview() {
        if (currentUser == null) {
            Toast.makeText(this, "Please login to delete reviews", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show confirmation dialog
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this);
        builder.setTitle("Delete Review");
        builder.setMessage("Are you sure you want to delete your review? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            // Find and delete the user's review for this movie
            firestore.collection("reviews")
                    .whereEqualTo("userId", currentUser.getUid())
                    .whereEqualTo("movieId", movie.getId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            doc.getReference().delete()
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(this, "Review deleted successfully", Toast.LENGTH_SHORT).show();
                                        loadReviews(); // Reload reviews
                                        checkUserReview(); // Refresh UI
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "Error deleting review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error finding review to delete: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        builder.show();
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
                binding.watchTrailerButton.setText("Watch Trailer");

                if (response.isSuccessful() && response.body() != null) {
                    VideoResponse videoResponse = response.body();
                    List<Video> videos = videoResponse.getResults();

                    // Find the best trailer
                    Video bestTrailer = findBestTrailer(videos);

                    if (bestTrailer != null) {
                        String youtubeUrl = bestTrailer.getYouTubeUrl();
                        if (youtubeUrl != null) {
                            showTrailerPopup(youtubeUrl);
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
                binding.watchTrailerButton.setText("Watch Trailer");
                fallbackToHomepage();
            }
        });
    }

    private void showTrailerPopup(String youtubeUrl) {
        // Create centered trailer popup dialog
        Dialog trailerDialog = new Dialog(this);
        trailerDialog.setContentView(R.layout.dialog_trailer);
        trailerDialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        trailerDialog.getWindow().setGravity(Gravity.CENTER);

        WebView trailerWebView = trailerDialog.findViewById(R.id.trailer_webview);
        ProgressBar progressBar = trailerDialog.findViewById(R.id.progress_bar);
        ImageButton closeButton = trailerDialog.findViewById(R.id.close_button);

        if (trailerWebView != null) {
            setupTrailerWebView(trailerWebView, progressBar);

            String embedUrl = convertToEmbedUrl(youtubeUrl);
            trailerWebView.loadUrl(embedUrl);
        }

        if (closeButton != null) {
            closeButton.setOnClickListener(v -> trailerDialog.dismiss());
        }

        trailerDialog.show();
    }

    private void setupTrailerWebView(WebView webView, ProgressBar progressBar) {
        android.webkit.WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setSupportZoom(false);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new android.webkit.WebViewClient() {
            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        webView.setWebChromeClient(new android.webkit.WebChromeClient() {
            @Override
            public void onProgressChanged(android.webkit.WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (progressBar != null) {
                    progressBar.setProgress(newProgress);
                }
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

    private String convertToEmbedUrl(String youtubeUrl) {
        if (youtubeUrl.contains("youtube.com/watch?v=")) {
            String videoId = youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
            if (videoId.contains("&")) {
                videoId = videoId.substring(0, videoId.indexOf("&"));
            }
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&modestbranding=1&rel=0";
        } else if (youtubeUrl.contains("youtu.be/")) {
            String videoId = youtubeUrl.substring(youtubeUrl.lastIndexOf("/") + 1);
            return "https://www.youtube.com/embed/" + videoId + "?autoplay=1&modestbranding=1&rel=0";
        }
        return youtubeUrl;
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
