package com.example.popflix;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.example.popflix.databinding.ActivityDetailsBinding;
import com.example.popflix.utils.WatchlistManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.auth.FirebaseAuth;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {
    
    private ActivityDetailsBinding binding;
    private Movie movie;
    private WatchlistManager watchlistManager;
    private boolean isInWatchlist = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize WatchlistManager
        watchlistManager = new WatchlistManager();

        // Get movie data from intent
        movie = (Movie) getIntent().getSerializableExtra("movie");
        if (movie == null) {
            Toast.makeText(this, "Movie data not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupWatchlistButton();
        loadMovieDetails();
        checkWatchlistStatus();
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

    private void loadMovieDetails() {
        // Set movie title
        binding.movieTitle.setText(movie.getTitle());

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
        binding.moviePlot.setText(movie.getOverview());

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
            binding.addToWatchlistButton.setIcon(getResources().getDrawable(R.drawable.ic_check));
        } else {
            binding.addToWatchlistButton.setText("Add to Watchlist");
            binding.addToWatchlistButton.setIcon(getResources().getDrawable(R.drawable.ic_add));
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
