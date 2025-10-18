package com.example.popflix;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.popflix.databinding.ActivitySearchResultsBinding;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class SearchResultsActivity extends AppCompatActivity {

    private ActivitySearchResultsBinding binding;
    private ApiService apiService;
    private SearchResultsAdapter adapter;
    private List<Movie> searchResults;
    private String searchQuery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySearchResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get search query from intent
        searchQuery = getIntent().getStringExtra("query");
        
        // Initialize components
        apiService = ApiClient.getClient(this);
        searchResults = new ArrayList<>();

        setupToolbar();
        setupRecyclerView();
        setupSearchInterface();
        
        // Only perform search if query is not empty
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            performSearch();
        }
    }

    private void setupSearchInterface() {
        // Add search input field to the layout
        binding.searchInput.setHint("Search movies, TV shows...");
        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            String query = binding.searchInput.getText().toString().trim();
            if (!query.isEmpty()) {
                searchQuery = query;
                performSearch();
            }
            return true;
        });
        
        // Focus on search input if no query provided
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            binding.searchInput.requestFocus();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Search Results");
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        adapter = new SearchResultsAdapter(searchResults, this::onMovieClick);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void performSearch() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.emptyState.setVisibility(View.GONE);

        Call<MovieResponse> call = apiService.searchMovies(searchQuery);
        call.enqueue(new Callback<MovieResponse>() {
            @Override
            public void onResponse(Call<MovieResponse> call, Response<MovieResponse> response) {
                binding.progressBar.setVisibility(View.GONE);
                
                if (response.isSuccessful() && response.body() != null) {
                    List<Movie> movies = response.body().getMovies();
                    if (!movies.isEmpty()) {
                        searchResults.clear();
                        searchResults.addAll(movies);
                        adapter.notifyDataSetChanged();
                        binding.resultCount.setText(movies.size() + " results found");
                        binding.resultCount.setVisibility(View.VISIBLE);
                    } else {
                        showEmptyState();
                    }
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<MovieResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                showEmptyState();
                Toast.makeText(SearchResultsActivity.this, "Search failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEmptyState() {
        binding.emptyState.setVisibility(View.VISIBLE);
        binding.resultCount.setVisibility(View.GONE);
    }

    private void onMovieClick(Movie movie) {
        // Navigate to details
        android.content.Intent intent = new android.content.Intent(this, DetailsActivity.class);
        intent.putExtra("movie", movie);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
