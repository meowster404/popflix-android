package com.example.popflix;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private View emptyState;
    private TextView favoritesCount;
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;
    private MovieAdapter adapter;
    private List<Movie> favoritesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorites);

        // Initialize Firebase components
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Please login to view favorites", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        loadFavorites();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Favorites");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycler_view);
        emptyState = findViewById(R.id.empty_state);
        favoritesCount = findViewById(R.id.favorites_count);

        favoritesList = new ArrayList<>();
        adapter = new MovieAdapter(favoritesList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void loadFavorites() {
        firestore.collection("favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    favoritesList.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        // Convert document data to Movie object with complete data
                        Movie movie = new Movie();
                        movie.setId(((Number) doc.get("movieId")).intValue());
                        movie.setTitle((String) doc.get("movieTitle"));
                        movie.setPosterPath((String) doc.get("moviePoster"));
                        movie.setOverview((String) doc.get("movieOverview"));
                        movie.setReleaseDate((String) doc.get("movieReleaseDate"));
                        movie.setBackdropPath((String) doc.get("movieBackdrop"));
                        
                        // Set default values for missing data
                        if (doc.get("movieVoteAverage") != null) {
                            movie.setVoteAverage(((Number) doc.get("movieVoteAverage")).doubleValue());
                        } else {
                            movie.setVoteAverage(0.0);
                        }
                        
                        if (doc.get("movieVoteCount") != null) {
                            movie.setVoteCount(((Number) doc.get("movieVoteCount")).intValue());
                        } else {
                            movie.setVoteCount(0);
                        }
                        
                        favoritesList.add(movie);
                    }

                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading favorites: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    updateEmptyState();
                });
    }

    private void updateEmptyState() {
        if (favoritesList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyState.setVisibility(View.VISIBLE);
            favoritesCount.setText("0 movies");
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyState.setVisibility(View.GONE);
            favoritesCount.setText(favoritesList.size() + " movies");
        }
    }
}
