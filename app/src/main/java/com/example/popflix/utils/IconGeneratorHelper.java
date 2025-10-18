package com.example.popflix.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Icon Generation Helper
 * Demonstrates how to use ImageGenerator to create custom app icons
 */
public class IconGeneratorHelper {

    private static final String TAG = "IconGeneratorHelper";

    /**
     * Generates and saves custom launcher icons for the app
     */
    public static boolean generateAndSaveLauncherIcons(Context context) {
        try {
            // Define icon sizes for different screen densities
            int[] sizes = {
                48,  // mdpi
                72,  // hdpi
                96,  // xhdpi
                144, // xxhdpi
                192  // xxxhdpi
            };

            String[] densityFolders = {
                "mipmap-mdpi",
                "mipmap-hdpi",
                "mipmap-xhdpi",
                "mipmap-xxhdpi",
                "mipmap-xxxhdpi"
            };

            // Get external files directory or create in app's internal storage
            File iconDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PopFlixIcons");
            if (!iconDir.exists()) {
                iconDir.mkdirs();
            }

            for (int i = 0; i < sizes.length; i++) {
                int size = sizes[i];
                String density = densityFolders[i];

                // Generate square launcher icon
                Bitmap squareIcon = ImageGenerator.generateLauncherIcon(size);

                // Generate round launcher icon
                Bitmap roundIcon = ImageGenerator.generateRoundLauncherIcon(size);

                // Save icons
                File squareFile = new File(iconDir, "ic_launcher_" + density + ".png");
                File roundFile = new File(iconDir, "ic_launcher_round_" + density + ".png");

                boolean squareSaved = ImageGenerator.saveBitmapToFile(squareIcon, squareFile);
                boolean roundSaved = ImageGenerator.saveBitmapToFile(roundIcon, roundFile);

                if (squareSaved && roundSaved) {
                    Log.d(TAG, "Successfully generated icons for " + density + " (" + size + "x" + size + ")");

                    // You could also copy these to res/mipmap folders programmatically
                    // copyIconToMipmap(context, squareFile, density, "ic_launcher");
                    // copyIconToMipmap(context, roundFile, density, "ic_launcher_round");
                } else {
                    Log.e(TAG, "Failed to save icons for " + density);
                    return false;
                }
            }

            Log.d(TAG, "All launcher icons generated successfully!");
            Log.d(TAG, "Icons saved to: " + iconDir.getAbsolutePath());
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error generating launcher icons: " + e.getMessage());
            return false;
        }
    }

    /**
     * Generates a custom app theme background
     */
    public static Bitmap generateAppThemeBackground(int width, int height) {
        return ImageGenerator.generateMoviePatternBackground(width, height);
    }

    /**
     * Generates a custom profile picture placeholder
     */
    public static Bitmap generateCustomProfilePlaceholder(int size) {
        return ImageGenerator.generateProfilePlaceholder(size);
    }

    /**
     * Generates adaptive icon components
     */
    public static class AdaptiveIconComponents {
        public static Bitmap generateBackground(int size) {
            return ImageGenerator.generateAdaptiveIconBackground(size);
        }

        public static Bitmap generateForeground(int size) {
            return ImageGenerator.generateAdaptiveIconForeground(size);
        }
    }

    /**
     * Creates a simple demo activity to showcase generated images
     */
    public static void demonstrateImageGeneration(Context context) {
        try {
            // Generate sample images
            Bitmap splashBg = ImageGenerator.generateSplashBackground(800, 600);
            Bitmap profilePlaceholder = ImageGenerator.generateProfilePlaceholder(200);
            Bitmap notificationIcon = ImageGenerator.generateNotificationIcon(96);

            // Save sample images
            File demoDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "PopFlixDemo");
            if (!demoDir.exists()) {
                demoDir.mkdirs();
            }

            ImageGenerator.saveBitmapToFile(splashBg,
                new File(demoDir, "splash_background.png"));
            ImageGenerator.saveBitmapToFile(profilePlaceholder,
                new File(demoDir, "profile_placeholder.png"));
            ImageGenerator.saveBitmapToFile(notificationIcon,
                new File(demoDir, "notification_icon.png"));

            Log.d(TAG, "Demo images generated and saved to: " + demoDir.getAbsolutePath());

        } catch (Exception e) {
            Log.e(TAG, "Error in demonstration: " + e.getMessage());
        }
    }

    /**
     * Quick integration example for the main app
     * Call this from any activity to generate custom images
     */
    public static void quickDemo(Context context) {
        try {
            // Generate a custom launcher icon and show it
            Bitmap customIcon = ImageGenerator.generateLauncherIcon(128);

            // Save it for use as a notification icon or other purposes
            File demoFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                                   "quick_demo_icon.png");
            ImageGenerator.saveBitmapToFile(customIcon, demoFile);

            android.util.Log.d(TAG, "Quick demo completed - icon saved to: " + demoFile.getAbsolutePath());

        } catch (Exception e) {
            android.util.Log.e(TAG, "Quick demo failed: " + e.getMessage());
        }
    }
}
