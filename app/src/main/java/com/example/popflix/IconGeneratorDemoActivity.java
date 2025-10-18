package com.example.popflix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.popflix.databinding.ActivityIconGeneratorDemoBinding;
import com.example.popflix.utils.IconGeneratorHelper;
import com.example.popflix.utils.ImageGenerator;

/**
 * Demo Activity to showcase programmatically generated images
 * This demonstrates the ImageGenerator utility capabilities
 */
public class IconGeneratorDemoActivity extends AppCompatActivity {

    private ActivityIconGeneratorDemoBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIconGeneratorDemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupDemo();
    }

    private void setupDemo() {
        // Generate and display sample images
        displaySampleImages();

        // Setup buttons
        binding.generateIconsButton.setOnClickListener(v -> generateAllIcons());
        binding.generateBackgroundButton.setOnClickListener(v -> showCustomBackground());
        binding.demoImagesButton.setOnClickListener(v -> showDemoImages());
    }

    private void displaySampleImages() {
        try {
            // Generate sample launcher icon
            Bitmap launcherIcon = ImageGenerator.generateLauncherIcon(200);
            binding.sampleLauncherIcon.setImageBitmap(launcherIcon);

            // Generate sample profile placeholder
            Bitmap profilePlaceholder = ImageGenerator.generateProfilePlaceholder(150);
            binding.sampleProfilePlaceholder.setImageBitmap(profilePlaceholder);

            // Generate sample notification icon
            Bitmap notificationIcon = ImageGenerator.generateNotificationIcon(100);
            binding.sampleNotificationIcon.setImageBitmap(notificationIcon);

        } catch (Exception e) {
            Toast.makeText(this, "Error displaying sample images: " + e.getMessage(),
                          Toast.LENGTH_SHORT).show();
        }
    }

    private void generateAllIcons() {
        boolean success = IconGeneratorHelper.generateAndSaveLauncherIcons(this);

        if (success) {
            Toast.makeText(this, "All launcher icons generated successfully!",
                          Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Error generating icons. Check logs for details.",
                          Toast.LENGTH_SHORT).show();
        }
    }

    private void showCustomBackground() {
        // Generate and display a custom background
        Bitmap background = ImageGenerator.generateMoviePatternBackground(600, 400);
        binding.customBackgroundDemo.setImageBitmap(background);
        binding.customBackgroundDemo.setVisibility(View.VISIBLE);
    }

    private void showDemoImages() {
        // Run the demo image generation
        IconGeneratorHelper.demonstrateImageGeneration(this);
        Toast.makeText(this, "Demo images generated! Check app folder.", Toast.LENGTH_LONG).show();
    }
}
