package com.example.popflix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.popflix.databinding.ActivityWatchlistBinding;
import com.example.popflix.utils.WatchlistManager;
import com.example.popflix.utils.ThemeManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class WatchlistActivity extends AppCompatActivity {
    
    private ActivityWatchlistBinding binding;
    private WatchlistManager watchlistManager;
    private WatchlistAdapter adapter;
    private List<Movie> watchlistMovies;
    private TextView watchlistCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before super.onCreate
        ThemeManager.getInstance(this).applyTheme(this);
        
        super.onCreate(savedInstanceState);
        binding = ActivityWatchlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Check if user is logged in
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "Please login to view your watchlist", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize components
        watchlistManager = new WatchlistManager();
        watchlistMovies = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        loadWatchlistMovies();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("My Watchlist");
            }
            
            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        watchlistCount = findViewById(R.id.watchlist_count);

        adapter = new WatchlistAdapter(watchlistMovies, this::onMovieClick, this::onRemoveFromWatchlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadWatchlistMovies() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        watchlistManager.getWatchlistMovies(userId, new WatchlistManager.WatchlistMoviesCallback() {
            @Override
            public void onSuccess(List<Movie> movies) {
                binding.progressBar.setVisibility(View.GONE);
                watchlistMovies.clear();
                watchlistMovies.addAll(movies);
                adapter.notifyDataSetChanged();

                updateEmptyState();
                updateWatchlistCount();
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(WatchlistActivity.this, "Error loading watchlist: " + error, Toast.LENGTH_SHORT).show();
                updateEmptyState();
            }
        });
    }

    private void updateEmptyState() {
        if (watchlistMovies.isEmpty()) {
            binding.recyclerView.setVisibility(View.GONE);
            binding.emptyState.setVisibility(View.VISIBLE);
        } else {
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.emptyState.setVisibility(View.GONE);
        }
    }

    private void updateWatchlistCount() {
        if (watchlistCount != null) {
            watchlistCount.setText(watchlistMovies.size() + " movies");
        }
    }

    private void onMovieClick(Movie movie) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    private void onRemoveFromWatchlist(Movie movie) {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        watchlistManager.removeFromWatchlist(userId, movie.getId(), new WatchlistManager.WatchlistCallback() {
            @Override
            public void onSuccess(boolean result) {
                // Remove from local list and update UI
                int position = watchlistMovies.indexOf(movie);
                if (position != -1) {
                    watchlistMovies.remove(position);
                    adapter.notifyItemRemoved(position);

                    updateEmptyState();
                    updateWatchlistCount();
                }
                Toast.makeText(WatchlistActivity.this, "Removed from watchlist", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WatchlistActivity.this, "Error removing from watchlist: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the watchlist when returning to this activity
        loadWatchlistMovies();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
