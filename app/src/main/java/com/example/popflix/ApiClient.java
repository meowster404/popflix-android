package com.example.popflix;

import android.content.Context;
import android.content.res.AssetManager;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ApiClient {
    private static final String BASE_URL = "https://api.themoviedb.org/3/";
    private static Retrofit retrofit = null;
    private static String API_KEY = null;

    public static ApiService getClient() {
        return getClient(null);
    }

    public static ApiService getClient(Context context) {
        if (retrofit == null) {
            // Load API key from .env file if not already loaded
            if (API_KEY == null) {
                API_KEY = loadApiKeyFromEnv(context);
            }

            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
            httpClient.addInterceptor(loggingInterceptor);
            httpClient.addInterceptor(chain -> {
                Request original = chain.request();
                HttpUrl originalHttpUrl = original.url();

                HttpUrl url = originalHttpUrl.newBuilder()
                        .addQueryParameter("api_key", API_KEY)
                        .build();

                Request.Builder requestBuilder = original.newBuilder()
                        .url(url);

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });


            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

    private static String loadApiKeyFromEnv(Context context) {
        String apiKey = "3b8e66b62b76f7bd72722a5ce290a8a0"; // fallback default

        try {
            AssetManager assetManager;
            if (context != null) {
                assetManager = context.getAssets();
            } else {
                // If no context provided, we can't read assets, use fallback
                return apiKey;
            }

            InputStream inputStream = assetManager.open(".env");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TMDB_API_KEY=")) {
                    apiKey = line.substring(line.indexOf("\"") + 1, line.lastIndexOf("\""));
                    break;
                }
            }

            reader.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
            // Use fallback API key if .env file is not found or can't be read
        }

        return apiKey;
    }
}