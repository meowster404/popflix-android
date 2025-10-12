package com.example.popflix;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("movie/popular")
    Call<MovieResponse> getPopularMovies();
}