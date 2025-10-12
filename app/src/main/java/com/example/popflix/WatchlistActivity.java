package com.example.popflix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.popflix.databinding.ActivityWatchlistBinding;
import com.example.popflix.utils.WatchlistManager;
import com.google.firebase.auth.FirebaseAuth;
import java.util.ArrayList;
import java.util.List;

public class WatchlistActivity extends AppCompatActivity {
    
    private ActivityWatchlistBinding binding;
    private WatchlistManager watchlistManager;
    private WatchlistAdapter adapter;
    private List<Movie> watchlistMovies;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWatchlistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        watchlistManager = new WatchlistManager();
        watchlistMovies = new ArrayList<>();
        
        setupToolbar();
        setupRecyclerView();
        loadWatchlistMovies();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.my_watchlist));
        }
    }

    private void setupRecyclerView() {
        adapter = new WatchlistAdapter(watchlistMovies, this::onMovieClick, this::onRemoveFromWatchlist);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setAdapter(adapter);
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
                
                if (movies.isEmpty()) {
                    showEmptyState();
                } else {
                    hideEmptyState();
                }
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(WatchlistActivity.this, "Error loading watchlist: " + error, Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
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
                    
                    if (watchlistMovies.isEmpty()) {
                        showEmptyState();
                    }
                }
                Toast.makeText(WatchlistActivity.this, "Removed from watchlist", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(WatchlistActivity.this, "Error removing from watchlist: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        binding.emptyState.setVisibility(View.GONE);
        binding.recyclerView.setVisibility(View.VISIBLE);
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
