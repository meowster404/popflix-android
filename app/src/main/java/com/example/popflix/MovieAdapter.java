package com.example.popflix;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.popflix.databinding.ItemMovieBinding;
import com.example.popflix.utils.PerformanceOptimizer;
import com.example.popflix.utils.ThemeManager;

import java.io.Serializable;
import java.util.List;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieViewHolder> {
    
    private List<Movie> movies;
    
    public MovieAdapter(List<Movie> movies) {
        this.movies = movies;
    }
    
    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMovieBinding binding = ItemMovieBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MovieViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }
    
    @Override
    public int getItemCount() {
        return movies != null ? movies.size() : 0;
    }
    
    // Optimize data updates
    public void updateMovies(List<Movie> newMovies) {
        this.movies = newMovies;
        notifyDataSetChanged();
    }
    
    // Add method to clear data and free memory
    public void clearData() {
        if (movies != null) {
            movies.clear();
            notifyDataSetChanged();
        }
    }
    
    static class MovieViewHolder extends RecyclerView.ViewHolder {
        private final ItemMovieBinding binding;
        
        public MovieViewHolder(ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(Movie movie) {
            binding.movieTitle.setText(movie.getTitle());
            
            // Set movie details (year and rating)
            String year = movie.getYear();
            String rating = String.format("%.1f", movie.getVoteAverage());
            String details = year + " • " + rating;
            if (binding.movieDetails != null) {
                binding.movieDetails.setText(details);
            }
            
            // Apply theme-aware colors
            ThemeManager themeManager = ThemeManager.getInstance(binding.getRoot().getContext());
            
            // Set card background color based on theme
            binding.getRoot().setCardBackgroundColor(themeManager.getCardBackgroundColor(binding.getRoot().getContext()));
            
            // Set text colors based on theme
            binding.movieTitle.setTextColor(themeManager.getTextPrimaryColor(binding.getRoot().getContext()));
            if (binding.movieDetails != null) {
                binding.movieDetails.setTextColor(themeManager.getTextSecondaryColor(binding.getRoot().getContext()));
            }
            
            // Use optimized image loading
            String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
            PerformanceOptimizer.loadMoviePoster(
                binding.getRoot().getContext(), 
                posterUrl, 
                binding.moviePoster
            );
            
            // Set click listener with optimized intent creation
            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), DetailsActivity.class);
                intent.putExtra("movie", (Serializable) movie);
                v.getContext().startActivity(intent);
            });
        }
        
        // Clean up resources when view is recycled
        public void cleanup() {
            binding.moviePoster.setImageDrawable(null);
        }
    }
    
    @Override
    public void onViewRecycled(@NonNull MovieViewHolder holder) {
        super.onViewRecycled(holder);
        // Clean up resources to prevent memory leaks
        holder.cleanup();
    }
}
