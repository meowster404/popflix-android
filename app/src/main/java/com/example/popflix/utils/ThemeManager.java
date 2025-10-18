package com.example.popflix.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.example.popflix.R;

/**
 * Theme manager for PopFlix app
 * Handles kid mode theming and general theme changes
 */
public class ThemeManager {
    
    private static final String PREF_NAME = "theme_preferences";
    private static final String KEY_KID_MODE = "kid_mode_enabled";
    private static final String KEY_THEME_MODE = "theme_mode";
    
    public static final int THEME_LIGHT = 0;
    public static final int THEME_DARK = 1;
    public static final int THEME_KID = 2;
    public static final int THEME_SYSTEM = 3;
    
    private static ThemeManager instance;
    private SharedPreferences preferences;
    
    private ThemeManager(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }
    
    public static ThemeManager getInstance(Context context) {
        if (instance == null) {
            instance = new ThemeManager(context.getApplicationContext());
        }
        return instance;
    }
    
    public void setKidModeEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_KID_MODE, enabled).apply();
        if (enabled) {
            setThemeMode(THEME_KID);
        } else {
            // Revert to previous theme or default dark
            setThemeMode(THEME_DARK);
        }
    }
    
    public boolean isKidModeEnabled() {
        return preferences.getBoolean(KEY_KID_MODE, false);
    }
    
    public void setThemeMode(int themeMode) {
        preferences.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }
    
    public int getThemeMode() {
        if (isKidModeEnabled()) {
            return THEME_KID;
        }
        int savedTheme = preferences.getInt(KEY_THEME_MODE, THEME_SYSTEM);
        if (savedTheme == THEME_SYSTEM) {
            // Detect system theme
            return isSystemDarkMode() ? THEME_DARK : THEME_LIGHT;
        }
        return savedTheme;
    }
    
    public void applyTheme(Activity activity) {
        if (activity instanceof AppCompatActivity) {
            applyTheme((AppCompatActivity) activity);
        } else {
            int themeMode = getThemeMode();

            switch (themeMode) {
                case THEME_LIGHT:
                    activity.setTheme(R.style.Theme_PopFlix_Light);
                    setStatusBarColor(activity, R.color.light_background);
                    break;
                case THEME_DARK:
                    activity.setTheme(R.style.Theme_PopFlix);
                    setStatusBarColor(activity, R.color.dark_background);
                    break;
                case THEME_KID:
                    activity.setTheme(R.style.Theme_PopFlix_Kid);
                    setStatusBarColor(activity, R.color.kid_primary);
                    break;
            }
        }
    }

    public void applyTheme(AppCompatActivity activity) {
        int themeMode = getThemeMode();

        switch (themeMode) {
            case THEME_LIGHT:
                activity.setTheme(R.style.Theme_PopFlix_Light);
                setStatusBarColor(activity, R.color.light_background);
                break;
            case THEME_DARK:
                activity.setTheme(R.style.Theme_PopFlix);
                setStatusBarColor(activity, R.color.dark_background);
                break;
            case THEME_KID:
                activity.setTheme(R.style.Theme_PopFlix_Kid);
                setStatusBarColor(activity, R.color.kid_primary);
                break;
        }
    }

    private void setStatusBarColor(Activity activity, int colorRes) {
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(activity, colorRes));

        // Set navigation bar color for better consistency
        window.setNavigationBarColor(ContextCompat.getColor(activity, colorRes));
    }

    private boolean isSystemDarkMode() {
        // For now, we'll use a simple check - can be improved later
        // Check if system is in dark mode using the current context
        try {
            // We'll use a basic check for now - in a real implementation,
            // you'd get the context from the activity calling this method
            return false; // Default to light mode for system default
        } catch (Exception e) {
            return false; // Default to light mode if we can't determine
        }
    }

    public int getPrimaryColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.primary_purple);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.brand_red);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_primary);
            default:
                return ContextCompat.getColor(context, R.color.brand_red);
        }
    }
    
    public int getSecondaryColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.light_secondary);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.dark_secondary);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_secondary);
            default:
                return ContextCompat.getColor(context, R.color.dark_secondary);
        }
    }
    
    public int getBackgroundColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.light_background);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.dark_background);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_background);
            default:
                return ContextCompat.getColor(context, R.color.dark_background);
        }
    }
    
    public int getCardBackgroundColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.card_background);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.card_background_dark);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_card_background);
            default:
                return ContextCompat.getColor(context, R.color.card_background_dark);
        }
    }
    
    public int getTextPrimaryColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.text_primary);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.text_primary_dark);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_text_primary);
            default:
                return ContextCompat.getColor(context, R.color.text_primary_dark);
        }
    }
    
    public int getTextSecondaryColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.text_secondary);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.text_secondary_dark);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_text_secondary);
            default:
                return ContextCompat.getColor(context, R.color.text_secondary_dark);
        }
    }

    public int getTextColor(Context context) {
        int themeMode = getThemeMode();
        switch (themeMode) {
            case THEME_LIGHT:
                return ContextCompat.getColor(context, R.color.light_text_primary);
            case THEME_DARK:
                return ContextCompat.getColor(context, R.color.text_primary);
            case THEME_KID:
                return ContextCompat.getColor(context, R.color.kid_text_primary);
            default:
                return ContextCompat.getColor(context, R.color.text_primary);
        }
    }
    
    /**
     * Apply kid-friendly colors to the entire app
     */
    public void applyKidModeColors(Activity activity) {
        if (!isKidModeEnabled()) return;

        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(ContextCompat.getColor(activity, R.color.kid_background));
        }

        // Apply kid mode colors to status bar and navigation bar
        setStatusBarColor(activity, R.color.kid_primary);
    }
    
    /**
     * Get kid-friendly greeting messages
     */
    public String getKidModeGreeting() {
        String[] greetings = {
            "Hey there, little movie explorer! 🎬",
            "Welcome to your magical movie world! ✨",
            "Ready for some awesome adventures? 🌟",
            "Let's discover amazing stories together! 🎭",
            "Time for fun and fantastic movies! 🎪"
        };
        
        int randomIndex = (int) (Math.random() * greetings.length);
        return greetings[randomIndex];
    }
    
    /**
     * Check if content is kid-friendly based on rating
     */
    public boolean isKidFriendlyContent(String rating) {
        if (rating == null) return false;
        
        // Kid-friendly ratings
        String[] kidRatings = {"G", "PG", "TV-Y", "TV-Y7", "TV-G", "E"};
        
        for (String kidRating : kidRatings) {
            if (rating.toUpperCase().contains(kidRating)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get kid mode filter for API calls
     */
    public String getKidModeGenreFilter() {
        // Return kid-friendly genre IDs for TMDB API
        return "16,10751,14,35"; // Animation, Family, Fantasy, Comedy
    }
}
