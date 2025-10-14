package com.example.popflix.home;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.example.popflix.ApiClient;
import com.example.popflix.ApiService;
import com.example.popflix.AuthActivity;
import com.example.popflix.Movie;
import com.example.popflix.MovieAdapter;
import com.example.popflix.MovieResponse;
import com.example.popflix.R;
import com.example.popflix.WatchlistActivity;
import com.example.popflix.databinding.ActivityHomeBinding;
import com.example.popflix.utils.WatchlistManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private ActivityHomeBinding binding;
    private ApiService apiService;
    private WatchlistManager watchlistManager;
    private Handler sliderHandler;
    private Runnable sliderRunnable;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize components
        apiService = ApiClient.getClient(this);
        watchlistManager = new WatchlistManager();

        // Initialize Firebase user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // User not logged in, redirect to auth
            Intent intent = new Intent(this, AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setupToolbar();
        setupNavigationDrawer();
        loadFeaturedMovies();
        loadPopularMovies();
        loadTopRatedMovies();
        loadUpcomingMovies();

        // Setup auto-scrolling featured movies
        setupAutoScrollFeatured();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void setupNavigationDrawer() {
        drawerLayout = binding.drawerLayout;
        navigationView = binding.navigationView;

        // Setup navigation header with user info
        setupNavigationHeader();

        // Setup navigation item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile settings coming soon!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_watchlist) {
                openWatchlist();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_favorites) {
                openFavorites();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_settings) {
                // Open settings activity
                Toast.makeText(this, "Settings coming soon!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                logout();
                return true;
            }

            return false;
        });
    }

    private void setupNavigationHeader() {
        View headerView = navigationView.getHeaderView(0);
        ImageView userPhoto = headerView.findViewById(R.id.nav_user_photo);
        TextView userName = headerView.findViewById(R.id.nav_user_name);
        TextView userEmail = headerView.findViewById(R.id.nav_user_email);

        if (currentUser != null) {
            userName.setText(currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
            userEmail.setText(currentUser.getEmail());

            // Load profile picture if available
            if (currentUser.getPhotoUrl() != null) {
                Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(userPhoto);
            } else {
                userPhoto.setImageResource(R.drawable.ic_profile);
            }

            // Make header clickable to open profile settings
            headerView.setOnClickListener(v -> {
                Toast.makeText(this, "Profile settings coming soon!", Toast.LENGTH_SHORT).show();
                drawerLayout.closeDrawer(GravityCompat.START);
            });
        }
    }

    private void openFavorites() {
        // For now, show a toast. Later we can implement a favorites activity
        Toast.makeText(this, "Favorites feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private void loadFeaturedMovies() {
        Call<MovieResponse> call = apiService.getNowPlayingMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupFeaturedSlider(response.body().getMovies());
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load featured movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupFeaturedSlider(List<Movie> movies) {
        if (movies.size() > 0) {
            FeaturedMoviesAdapter adapter = new FeaturedMoviesAdapter(movies);
            binding.featuredSlider.setAdapter(adapter);

            // Setup auto-scroll
            sliderHandler = new Handler(Looper.getMainLooper());
            sliderRunnable = new Runnable() {
                @Override
                public void run() {
                    int currentItem = binding.featuredSlider.getCurrentItem();
                    int nextItem = (currentItem + 1) % movies.size();
                    binding.featuredSlider.setCurrentItem(nextItem, true);
                    sliderHandler.postDelayed(this, 4000); // Change slide every 4 seconds
                }
            };
            sliderHandler.postDelayed(sliderRunnable, 4000);
        }
    }

    private void loadPopularMovies() {
        Call<MovieResponse> call = apiService.getPopularMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerPopular, "Popular Movies");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load popular movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTopRatedMovies() {
        Call<MovieResponse> call = apiService.getTopRatedMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerTopRated, "Top Rated");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load top rated movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUpcomingMovies() {
        Call<MovieResponse> call = apiService.getUpcomingMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerUpcoming, "Upcoming Movies");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load upcoming movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMovieRecyclerView(List<Movie> movies, RecyclerView recyclerView, String categoryTitle) {
        if (movies.size() > 0) {
            MovieAdapter adapter = new MovieAdapter(movies);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.setAdapter(adapter);

            // Add category title
            TextView categoryTitleView = findViewById(getCategoryTitleId(recyclerView.getId()));
            if (categoryTitleView != null) {
                categoryTitleView.setText(categoryTitle);
                categoryTitleView.setVisibility(View.VISIBLE);
            }
        }
    }

    private int getCategoryTitleId(int recyclerViewId) {
        if (recyclerViewId == R.id.recycler_popular) return R.id.text_popular_title;
        if (recyclerViewId == R.id.recycler_top_rated) return R.id.text_top_rated_title;
        if (recyclerViewId == R.id.recycler_upcoming) return R.id.text_upcoming_title;
        return 0;
    }

    private void setupAutoScrollFeatured() {
        // Auto-scroll is handled in setupFeaturedSlider
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
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
        if (sliderHandler != null && sliderRunnable != null) {
            sliderHandler.removeCallbacks(sliderRunnable);
        }
    }
}
