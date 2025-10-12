package com.example.popflix;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.example.popflix.databinding.ActivityDetailsBinding;
import com.google.android.material.chip.Chip;

import java.util.Arrays;
import java.util.List;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();

        // Example data - In a real app, you would pass a movie ID and fetch data
        populateUiWithExampleData();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void populateUiWithExampleData() {
        // This is placeholder data. You would fetch this from your ViewModel/Repository.
        binding.collapsingToolbar.setTitle("Example Movie Title");
        binding.movieTitle.setText("Example Movie Title");
        binding.movieYear.setText("2025 • ");
        binding.movieRuntime.setText("2h 5m • ");
        binding.movieRating.setText("8.1");
        binding.moviePlot.setText("This is a compelling plot summary that entices the user to watch the trailer. It details the exciting premise and introduces the main characters and the central conflict they will face on their epic journey.");

        Glide.with(this)
                .load("https://image.tmdb.org/t/p/w1280/fqv8v6AycXbS7exX7wbxIIzT6A2.jpg") // Example backdrop
                .into(binding.backdropImage);

        List<String> genres = Arrays.asList("Action", "Sci-Fi", "Adventure");
        for (String genreName : genres) {
            Chip chip = new Chip(this);
            chip.setText(genreName);
            binding.genreChips.addView(chip);
        }
    }
}
