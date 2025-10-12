package com.example.popflix.network;

import com.example.popflix.model.Movie;
import com.example.popflix.model.MovieResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies(@Query("page") int page);

    @GET("movie/top_rated")
    Call<MovieResponse> getTopRatedMovies(@Query("page") int page);

    @GET("movie/upcoming")
    Call<MovieResponse> getUpcomingMovies(@Query("page") int page);

    @GET("movie/{movie_id}")
    Call<Movie> getMovieDetails(@Path("movie_id") int movieId);
}