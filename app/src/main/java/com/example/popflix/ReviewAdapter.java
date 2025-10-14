package com.example.popflix;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private List<Review> reviews;

    public ReviewAdapter(List<Review> reviews) {
        this.reviews = reviews;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private TextView userNameText;
        private TextView reviewDateText;
        private RatingBar ratingBar;
        private TextView reviewContentText;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameText = itemView.findViewById(R.id.review_user_name);
            reviewDateText = itemView.findViewById(R.id.review_date);
            ratingBar = itemView.findViewById(R.id.review_rating);
            reviewContentText = itemView.findViewById(R.id.review_content);
        }

        public void bind(Review review) {
            userNameText.setText(review.getUserName());
            reviewDateText.setText(review.getFormattedDate());
            ratingBar.setRating(review.getRating());
            reviewContentText.setText(review.getContent());
        }
    }
}
