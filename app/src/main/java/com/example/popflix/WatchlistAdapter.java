package com.example.popflix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.popflix.databinding.ItemWatchlistMovieBinding;
import java.util.List;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {
    
    private List<Movie> movies;
    private OnMovieClickListener onMovieClickListener;
    private OnRemoveClickListener onRemoveClickListener;
    
    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }
    
    public interface OnRemoveClickListener {
        void onRemoveClick(Movie movie);
    }
    
    public WatchlistAdapter(List<Movie> movies, OnMovieClickListener onMovieClickListener, OnRemoveClickListener onRemoveClickListener) {
        this.movies = movies;
        this.onMovieClickListener = onMovieClickListener;
        this.onRemoveClickListener = onRemoveClickListener;
    }
    
    @NonNull
    @Override
    public WatchlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemWatchlistMovieBinding binding = ItemWatchlistMovieBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new WatchlistViewHolder(binding);
    }
    
    @Override
    public void onBindViewHolder(@NonNull WatchlistViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }
    
    @Override
    public int getItemCount() {
        return movies.size();
    }
    
    class WatchlistViewHolder extends RecyclerView.ViewHolder {
        private ItemWatchlistMovieBinding binding;
        
        public WatchlistViewHolder(@NonNull ItemWatchlistMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(Movie movie) {
            // Set movie title
            binding.movieTitle.setText(movie.getTitle());
            
            // Set movie year and rating
            String year = movie.getYear();
            String rating = movie.getFormattedRating();
            String details = year + " • " + rating;
            binding.movieDetails.setText(details);
            
            // Load poster image
            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                Glide.with(binding.getRoot().getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.moviePoster);
            }
            
            // Set click listeners
            binding.getRoot().setOnClickListener(v -> {
                if (onMovieClickListener != null) {
                    onMovieClickListener.onMovieClick(movie);
                }
            });
            
            binding.removeButton.setOnClickListener(v -> {
                if (onRemoveClickListener != null) {
                    onRemoveClickListener.onRemoveClick(movie);
                }
            });
        }
    }
}
