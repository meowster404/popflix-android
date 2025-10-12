// app/src/main/java/com/example/popflix/splash/SplashActivity.java
package com.example.popflix.splash;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;

import com.example.popflix.auth.AuthActivity;
import com.example.popflix.home.HomeActivity;
import com.google.firebase.auth.FirebaseAuth;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private static final int SPLASH_DURATION = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // The layout is set by the theme in styles.xml

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            // Check if user is signed in (non-null) and update UI accordingly.
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                // User is signed in, navigate to Home
                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
            } else {
                // No user is signed in, navigate to Auth
                startActivity(new Intent(SplashActivity.this, AuthActivity.class));
            }
            finish(); // Finish SplashActivity so user can't go back to it
        }, SPLASH_DURATION);
    }
}
