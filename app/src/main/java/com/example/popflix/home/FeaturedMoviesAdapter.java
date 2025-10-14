package com.example.popflix.home;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.popflix.DetailsActivity;
import com.example.popflix.Movie;
import com.example.popflix.R;

import java.util.List;

public class FeaturedMoviesAdapter extends RecyclerView.Adapter<FeaturedMoviesAdapter.FeaturedMovieViewHolder> {

    private List<Movie> movies;

    public FeaturedMoviesAdapter(List<Movie> movies) {
        this.movies = movies;
    }

    @NonNull
    @Override
    public FeaturedMovieViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_featured_movie, parent, false);
        return new FeaturedMovieViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedMovieViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    static class FeaturedMovieViewHolder extends RecyclerView.ViewHolder {
        private ImageView backdropImage;
        private TextView titleText;
        private TextView overviewText;
        private TextView ratingText;

        public FeaturedMovieViewHolder(@NonNull View itemView) {
            super(itemView);
            backdropImage = itemView.findViewById(R.id.backdrop_image);
            titleText = itemView.findViewById(R.id.movie_title);
            overviewText = itemView.findViewById(R.id.movie_overview);
            ratingText = itemView.findViewById(R.id.movie_rating);
        }

        public void bind(Movie movie) {
            Context context = itemView.getContext();

            // Load backdrop image
            if (movie.getBackdropPath() != null && !movie.getBackdropPath().isEmpty()) {
                String backdropUrl = "https://image.tmdb.org/t/p/w1280" + movie.getBackdropPath();
                Glide.with(context)
                        .load(backdropUrl)
                        .into(backdropImage);
            }

            // Set title
            titleText.setText(movie.getTitle());

            // Set overview (truncated)
            String overview = movie.getOverview();
            if (overview != null && overview.length() > 100) {
                overview = overview.substring(0, 100) + "...";
            }
            overviewText.setText(overview);

            // Set rating
            ratingText.setText(String.format("★ %.1f", movie.getVoteAverage()));

            // Set click listener
            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("movie", movie);
                context.startActivity(intent);
            });
        }
    }
}
