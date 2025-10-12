package com.example.popflix.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.popflix.ApiClient;
import com.example.popflix.ApiService;
import com.example.popflix.AuthActivity;
import com.example.popflix.Movie;
import com.example.popflix.MovieResponse;
import com.example.popflix.R;
import com.example.popflix.MovieAdapter;
import com.example.popflix.WatchlistActivity;
import com.example.popflix.databinding.ActivityHomeBinding;
import com.example.popflix.utils.WatchlistManager;
import com.google.firebase.auth.FirebaseAuth;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {
    
    private ActivityHomeBinding binding;
    private ApiService apiService;
    private WatchlistManager watchlistManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        apiService = ApiClient.getClient();
        watchlistManager = new WatchlistManager();
        
        setupToolbar();
        loadPopularMovies();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void loadPopularMovies() {
        binding.progressBar.setVisibility(View.VISIBLE);
        
        Call<MovieResponse> call = apiService.getPopularMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    MovieResponse movieResponse = response.body();
                    setupMovieRecyclerView(movieResponse.getMovies());
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMovieRecyclerView(java.util.List<Movie> movies) {
        MovieAdapter adapter = new MovieAdapter(movies);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerTodaysFeature.setLayoutManager(layoutManager);
        binding.recyclerTodaysFeature.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_watchlist) {
            openWatchlist();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    private void openWatchlist() {
        Intent intent = new Intent(this, WatchlistActivity.class);
        startActivity(intent);
    }

    private void logout() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
