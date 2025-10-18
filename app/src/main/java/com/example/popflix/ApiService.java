package com.example.popflix;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies();

    @GET("movie/now_playing")
    Call<MovieResponse> getNowPlayingMovies();

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies();

    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies();

    @GET("movie/{movie_id}/videos")
    Call<VideoResponse> getMovieVideos(@Path("movie_id") int movieId);

    @GET("movie/{movie_id}/reviews")
    Call<ReviewResponse> getMovieReviews(@Path("movie_id") int movieId);

    // New endpoints for 2025 features
    @GET("discover/movie?with_genres=16&sort_by=popularity.desc")
    Call<MovieResponse> getKidsMovies();

    @GET("trending/movie/week")
    Call<MovieResponse> getTrendingMovies();

    @GET("movie/now_playing?region=US")
    Call<MovieResponse> getLatestReleases();

    @GET("discover/movie?with_genres=28&sort_by=popularity.desc")
    Call<MovieResponse> getActionMovies();

    @GET("discover/movie?with_genres=35&sort_by=popularity.desc")
    Call<MovieResponse> getComedyMovies();

    @GET("search/movie")
    Call<MovieResponse> searchMovies(@Query("query") String query);
}