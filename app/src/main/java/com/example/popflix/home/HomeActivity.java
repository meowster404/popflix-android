package com.example.popflix.home;

import android.util.Base64;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.text.TextUtils;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.popflix.ApiClient;
import com.example.popflix.ApiService;
import com.example.popflix.AuthActivity;
import com.example.popflix.Movie;
import com.example.popflix.MovieResponse;
import com.example.popflix.R;
import com.example.popflix.SettingsActivity;
import com.example.popflix.FavoritesActivity;
import com.example.popflix.utils.ThemeManager;
import com.example.popflix.utils.PerformanceOptimizer;
import com.example.popflix.utils.FirebaseUserManager;
import com.example.popflix.ProfileActivity;
import com.example.popflix.MovieAdapter;
import com.example.popflix.WatchlistActivity;
import com.example.popflix.databinding.ActivityHomeBinding;
import com.example.popflix.utils.WatchlistManager;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
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
    private ThemeManager themeManager;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private FirebaseUser currentUser;
    private SharedPreferences preferences;
    private Handler searchHandler;
    private Runnable searchRunnable;
    private boolean isKidModeEnabled = false;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize theme manager and apply theme before calling super.onCreate
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase Auth and preferences
        FirebaseAuth auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        preferences = getSharedPreferences("app_preferences", MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
        
        // Create or update user in Firebase users table
        if (currentUser != null) {
            FirebaseUserManager.getInstance().createOrUpdateUser(currentUser, new FirebaseUserManager.UserUpdateCallback() {
                @Override
                public void onSuccess() {
                    Log.d("HomeActivity", "User data updated successfully");
                }
                
                @Override
                public void onError(String error) {
                    Log.e("HomeActivity", "Error updating user data: " + error);
                }
            });
        }
        
        // Get kid mode state from ThemeManager
        isKidModeEnabled = themeManager.isKidModeEnabled();
        
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Apply kid mode colors if enabled
        if (isKidModeEnabled) {
            themeManager.applyKidModeColors(this);
        }

        apiService = ApiClient.getClient();
        watchlistManager = new WatchlistManager();
        searchHandler = new Handler(Looper.getMainLooper());

        setupToolbar();
        setupCustomToolbar();
        setupNavigationDrawer();
        loadFeaturedMovies();
        loadPopularMovies();
        loadTopRatedMovies();
        loadUpcomingMovies();
        loadKidsMovies();
        loadTrendingMovies();
        loadLatestReleases();
        loadActionMovies();
        loadComedyMovies();

        // Setup auto-scrolling featured movies
        setupAutoScrollFeatured();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        }
    }

    private void setupCustomToolbar() {
        // Setup custom toolbar profile picture
        ImageView toolbarProfileImage = findViewById(R.id.toolbar_profile_image);
        View toolbarProfileCard = findViewById(R.id.toolbar_profile_card);
        ImageView toolbarSearchIcon = findViewById(R.id.toolbar_search_icon);

        if (toolbarProfileImage != null && currentUser != null) {
            // Use PopFlixPrefs for consistency
            SharedPreferences prefs = getSharedPreferences("PopFlixPrefs", MODE_PRIVATE);
            
            // FIRST: Load from cache immediately
            String cachedProfilePic = prefs.getString("custom_profile_pic", null);
            if (cachedProfilePic != null && !cachedProfilePic.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(cachedProfilePic, Base64.DEFAULT);
                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    toolbarProfileImage.setImageBitmap(decodedBitmap);
                } catch (Exception e) {
                    loadFirebaseProfilePicture(toolbarProfileImage);
                }
            } else {
                loadFirebaseProfilePicture(toolbarProfileImage);
            }
            
            // THEN: Update from Firestore in background
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String profilePicBase64 = documentSnapshot.getString("profilePicture");
                            if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                                try {
                                    byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    toolbarProfileImage.setImageBitmap(decodedBitmap);
                                    
                                    // Update cache
                                    prefs.edit().putString("custom_profile_pic", profilePicBase64).apply();
                                } catch (Exception e) {
                                    // Keep cached image
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeActivity", "Error loading toolbar profile from Firestore", e);
                        // Keep cached image
                    });

            // Set click listener to open sidebar (not profile page)
            if (toolbarProfileCard != null) {
                toolbarProfileCard.setOnClickListener(v -> {
                    if (drawerLayout != null) {
                        drawerLayout.openDrawer(GravityCompat.START);
                    }
                });
            }
        }

        // Setup search icon click
        if (toolbarSearchIcon != null) {
            toolbarSearchIcon.setOnClickListener(v -> {
                // Navigate to search activity
                Intent intent = new Intent(this, com.example.popflix.SearchResultsActivity.class);
                intent.putExtra("query", ""); // Empty query to show search interface
                startActivity(intent);
            });
        }
    }

    private void loadFirebaseProfilePictureForToolbar(ImageView toolbarProfileImage) {
        // Use FirebaseUserManager for better profile picture handling
        FirebaseUserManager.getInstance().loadProfilePicture(this, toolbarProfileImage, 100);
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
                openProfile();
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
                openSettings();
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            } else if (id == R.id.nav_logout) {
                logout();
                return true;
            } else if (id == R.id.nav_kid_mode) {
                toggleKidMode(item);
                return true;
            }

            return false;
        });
        
        // Set kid mode menu item state
        MenuItem kidModeItem = navigationView.getMenu().findItem(R.id.nav_kid_mode);
        if (kidModeItem != null) {
            kidModeItem.setChecked(isKidModeEnabled);
        }
    }

    private void setupNavigationHeader() {
        if (navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        ImageView userPhoto = headerView.findViewById(R.id.nav_user_photo);
        TextView userName = headerView.findViewById(R.id.nav_user_name);
        TextView userEmail = headerView.findViewById(R.id.nav_user_email);

        if (currentUser != null) {
            userEmail.setText(currentUser.getEmail());

            // FIRST: Load from cache immediately for instant display
            loadFromCache(userName, userPhoto);

            // THEN: Update from Firestore in background
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            // Get display name from Firestore
                            String displayName = documentSnapshot.getString("displayName");
                            if (displayName == null || displayName.isEmpty()) {
                                displayName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
                            }
                            userName.setText(displayName);

                            // Cache to SharedPreferences (using PopFlixPrefs for consistency)
                            SharedPreferences prefs = getSharedPreferences("PopFlixPrefs", MODE_PRIVATE);
                            prefs.edit().putString("user_display_name", displayName).apply();

                            // Get profile picture from Firestore
                            String profilePicBase64 = documentSnapshot.getString("profilePicture");
                            if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                                try {
                                    byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                    userPhoto.setImageBitmap(decodedBitmap);
                                    
                                    // Cache to SharedPreferences
                                    prefs.edit().putString("custom_profile_pic", profilePicBase64).apply();
                                } catch (Exception e) {
                                    loadFirebaseProfilePicture(userPhoto);
                                }
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e("HomeActivity", "Error loading profile from Firestore", e);
                        // Cache is already loaded, no need to reload
                    });

            // Make header clickable to open profile settings
            headerView.setOnClickListener(v -> {
                openProfile();
            });
        }
    }

    private void loadFromCache(TextView userName, ImageView userPhoto) {
        // Use PopFlixPrefs for consistency with ProfileActivity
        SharedPreferences prefs = getSharedPreferences("PopFlixPrefs", MODE_PRIVATE);
        
        // Load username from cache
        String savedName = prefs.getString("user_display_name", null);
        String displayName = savedName != null ? savedName : 
                           (currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User");
        userName.setText(displayName);

        // Load profile picture from cache
        String customProfilePic = prefs.getString("custom_profile_pic", null);
        if (customProfilePic != null && !customProfilePic.isEmpty()) {
            try {
                byte[] decodedBytes = Base64.decode(customProfilePic, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                userPhoto.setImageBitmap(decodedBitmap);
            } catch (Exception e) {
                loadFirebaseProfilePicture(userPhoto);
            }
        } else {
            loadFirebaseProfilePicture(userPhoto);
        }
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

    private void loadKidsMovies() {
        Call<MovieResponse> call = apiService.getKidsMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerKids, "Popular for Kids");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load kids movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTrendingMovies() {
        Call<MovieResponse> call = apiService.getTrendingMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerTrending, "Trending Now");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load trending movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadLatestReleases() {
        Call<MovieResponse> call = apiService.getLatestReleases();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerLatest, "Latest Releases");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load latest releases", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadActionMovies() {
        Call<MovieResponse> call = apiService.getActionMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerAction, "Action & Adventure");
                } else {
                    Toast.makeText(HomeActivity.this, "Failed to load action movies", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadComedyMovies() {
        Call<MovieResponse> call = apiService.getComedyMovies();
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    setupMovieRecyclerView(response.body().getMovies(), binding.recyclerComedy, "Comedy Movies");
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                Toast.makeText(HomeActivity.this, "Failed to load comedy movies", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMovieRecyclerView(List<Movie> movies, RecyclerView recyclerView, String title) {
        if (movies != null && !movies.isEmpty()) {
            MovieAdapter adapter = new MovieAdapter(movies);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            PerformanceOptimizer.optimizeRecyclerView(recyclerView);
        }
    }

    private int getCategoryTitleId(int recyclerViewId) {
        if (recyclerViewId == R.id.recycler_popular) return R.id.text_popular_title;
        if (recyclerViewId == R.id.recycler_top_rated) return R.id.text_top_rated_title;
        if (recyclerViewId == R.id.recycler_upcoming) return R.id.text_upcoming_title;
        if (recyclerViewId == R.id.recycler_kids) return R.id.text_kids_title;
        if (recyclerViewId == R.id.recycler_trending) return R.id.text_trending_title;
        if (recyclerViewId == R.id.recycler_latest) return R.id.text_latest_title;
        if (recyclerViewId == R.id.recycler_action) return R.id.text_action_title;
        if (recyclerViewId == R.id.recycler_comedy) return R.id.text_comedy_title;
        return 0;
    }

    private void setupAutoScrollFeatured() {
        // Auto-scroll is handled in setupFeaturedSlider
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        // No search menu item to setup since we use custom toolbar
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // No menu items to handle since we use custom toolbar
        return super.onOptionsItemSelected(item);
    }

    private void performLiveSearch(String query) {
        if (query.trim().isEmpty()) {
            return;
        }

        // Call TMDB search API for live search
        Call<MovieResponse> call = apiService.searchMovies(query);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> searchResults = response.body().getMovies();
                    if (!searchResults.isEmpty()) {
                        // Update the popular movies section with search results
                        setupMovieRecyclerView(searchResults, binding.recyclerPopular, "Search Results: " + query);
                    }
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                // Handle search failure silently for live search
            }
        });
    }

    private void performSearch(String query) {
        if (query.trim().isEmpty()) {
            Toast.makeText(this, "Please enter a search term", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to search results activity
        Intent intent = new Intent(this, com.example.popflix.SearchResultsActivity.class);
        intent.putExtra("query", query.trim());
        startActivity(intent);
    }

    private void openProfile() {
        Intent intent = new Intent(this, com.example.popflix.ProfileActivity.class);
        startActivity(intent);
    }

    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void openWatchlist() {
        Intent intent = new Intent(this, WatchlistActivity.class);
        startActivity(intent);
    }

    private void toggleKidMode(MenuItem item) {
        isKidModeEnabled = !isKidModeEnabled;
        item.setChecked(isKidModeEnabled);

        // Update ThemeManager
        themeManager.setKidModeEnabled(isKidModeEnabled);

        String statusMessage;
        if (isKidModeEnabled) {
            statusMessage = "🎈 " + themeManager.getKidModeGreeting() + " Kid Mode is now ACTIVE!";
            // Apply kid mode colors immediately
            themeManager.applyKidModeColors(this);
            // Load only kids content
            loadKidsContent();
        } else {
            statusMessage = "Kid Mode INACTIVE - All content available";
            // Reload all content
            loadAllContent();
        }

        Toast.makeText(this, statusMessage, Toast.LENGTH_LONG).show();

        // Apply theme changes without recreating the activity
        themeManager.applyTheme(this);

        drawerLayout.closeDrawer(GravityCompat.START);
    }

    private void loadKidsContent() {
        // Load only kids movies in all sections with proper filtering
        loadKidsMovies();
        // Filter other sections to show only G-rated content
        loadFilteredMovies("16,10751,14,35"); // Animation, Family, Fantasy, Comedy
    }

    private void loadFilteredMovies(String genres) {
        // This would require updating the API service to accept genre filtering
        // For now, we'll just load kids movies in all sections
        loadKidsMovies();
    }

    private void loadAllContent() {
        // Reload all movie sections with original filtering
        loadFeaturedMovies();
        loadPopularMovies();
        loadTopRatedMovies();
        loadUpcomingMovies();
        loadKidsMovies();
        loadTrendingMovies();
        loadLatestReleases();
        loadActionMovies();
        loadComedyMovies();
    }

    private void logout() {
        // Clear cached profile data
        SharedPreferences prefs = getSharedPreferences("PopFlixPrefs", MODE_PRIVATE);
        prefs.edit()
            .remove("user_display_name")
            .remove("custom_profile_pic")
            .apply();
        
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();
        
        // Navigate to auth screen
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void openFavorites() {
        Intent intent = new Intent(this, FavoritesActivity.class);
        startActivity(intent);
    }

    private void loadFirebaseProfilePicture(ImageView userPhoto) {
        // Use FirebaseUserManager for better profile picture handling
        FirebaseUserManager.getInstance().loadProfilePicture(this, userPhoto, 150);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh navigation header in case user updated their name
        setupNavigationHeader();
        setupCustomToolbar();
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
