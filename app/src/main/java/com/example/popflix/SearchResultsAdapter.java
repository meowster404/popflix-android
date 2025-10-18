package com.example.popflix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.SearchViewHolder> {

    private List<Movie> movies;
    private OnMovieClickListener listener;

    public interface OnMovieClickListener {
        void onMovieClick(Movie movie);
    }

    public SearchResultsAdapter(List<Movie> movies, OnMovieClickListener listener) {
        this.movies = movies;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        Movie movie = movies.get(position);
        holder.bind(movie);
    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    class SearchViewHolder extends RecyclerView.ViewHolder {
        private ImageView moviePoster;
        private TextView movieTitle;
        private TextView movieYear;
        private TextView movieType;

        public SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            moviePoster = itemView.findViewById(R.id.movie_poster);
            movieTitle = itemView.findViewById(R.id.movie_title);
            movieYear = itemView.findViewById(R.id.movie_year);
            movieType = itemView.findViewById(R.id.movie_type);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onMovieClick(movies.get(position));
                    }
                }
            });
        }

        public void bind(Movie movie) {
            movieTitle.setText(movie.getTitle());
            movieYear.setText(movie.getYear());
            movieType.setText("Movie"); // You can enhance this based on movie type

            // Load poster
            if (movie.getPosterPath() != null && !movie.getPosterPath().isEmpty()) {
                String posterUrl = "https://image.tmdb.org/t/p/w200" + movie.getPosterPath();
                Glide.with(itemView.getContext())
                        .load(posterUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(moviePoster);
            } else {
                moviePoster.setImageResource(R.drawable.ic_launcher_foreground);
            }
        }
    }
}
