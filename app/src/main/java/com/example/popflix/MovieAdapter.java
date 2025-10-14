package com.example.popflix;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.popflix.databinding.ItemMovieBinding;

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
        return movies.size();
    }
    
    class MovieViewHolder extends RecyclerView.ViewHolder {
        private ItemMovieBinding binding;
        
        public MovieViewHolder(@NonNull ItemMovieBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        
        public void bind(Movie movie) {
            // Set movie title
            if (movie.getTitle() != null) {
                binding.movieTitle.setText(movie.getTitle());
            }
            
            // Set movie year and rating
            String year = movie.getYear();
            String rating = movie.getFormattedRating();
            String details = year + " • " + rating;
            binding.movieDetails.setText(details);
            
            // Load poster image
            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                String posterUrl = "https://image.tmdb.org/t/p/w500" + movie.getPosterPath();
                if (binding.getRoot().getContext() != null) {
                    Glide.with(binding.getRoot().getContext())
                            .load(posterUrl)
                            .placeholder(R.drawable.ic_launcher_foreground)
                            .error(R.drawable.ic_launcher_foreground)
                            .into(binding.moviePoster);
                }
            }
            
            // Set click listener
            binding.getRoot().setOnClickListener(v -> {
                if (binding.getRoot().getContext() != null) {
                    Intent intent = new Intent(binding.getRoot().getContext(), DetailsActivity.class);
                    intent.putExtra("movie", movie);
                    binding.getRoot().getContext().startActivity(intent);
                }
            });
        }
    }
}
