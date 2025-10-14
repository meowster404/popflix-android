package com.example.popflix;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

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
}