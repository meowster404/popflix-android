package com.example.popflix;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.example.popflix.utils.ThemeManager;

public class SettingsActivity extends AppCompatActivity {

    private MaterialSwitch notificationsSwitch;
    private RadioGroup themeRadioGroup;
    private MaterialSwitch autoPlaySwitch;
    private LinearLayout aboutSection;
    private MaterialButton deleteAccountButton;
    private int aboutClickCount = 0;
    private ThemeManager themeManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Initialize and apply theme before calling super.onCreate
        themeManager = ThemeManager.getInstance(this);
        themeManager.applyTheme(this);
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupToolbar();
        setupSettings();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Settings");
            }

            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    private void setupSettings() {
        notificationsSwitch = findViewById(R.id.notifications_switch);
        themeRadioGroup = findViewById(R.id.theme_radio_group);
        autoPlaySwitch = findViewById(R.id.auto_play_switch);
        aboutSection = findViewById(R.id.about_section);
        deleteAccountButton = findViewById(R.id.delete_account_button);

        // Load saved preferences
        SharedPreferences preferences = getSharedPreferences("PopFlixSettings", MODE_PRIVATE);
        notificationsSwitch.setChecked(preferences.getBoolean("notifications_enabled", true));
        autoPlaySwitch.setChecked(preferences.getBoolean("auto_play_enabled", true));

        // Load theme preference from ThemeManager
        int themeMode = themeManager.getThemeMode();
        switch (themeMode) {
            case ThemeManager.THEME_LIGHT:
                themeRadioGroup.check(R.id.theme_light);
                break;
            case ThemeManager.THEME_DARK:
                themeRadioGroup.check(R.id.theme_dark);
                break;
            case ThemeManager.THEME_KID:
                // If kid mode is active, show it as a special state
                themeRadioGroup.check(R.id.theme_dark); // Default fallback
                break;
            default:
                themeRadioGroup.check(R.id.theme_system);
                break;
        }

        // Set up listeners
        notificationsSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("notifications_enabled", isChecked);
            Toast.makeText(this, "Notifications " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        themeRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int newThemeMode;
            String themeName;
            
            if (checkedId == R.id.theme_light) {
                newThemeMode = ThemeManager.THEME_LIGHT;
                themeName = "Light";
            } else if (checkedId == R.id.theme_dark) {
                newThemeMode = ThemeManager.THEME_DARK;
                themeName = "Dark";
            } else {
                newThemeMode = ThemeManager.THEME_DARK; // Default to dark for system
                themeName = "System Default";
            }
            
            // Update ThemeManager
            themeManager.setThemeMode(newThemeMode);
            
            Toast.makeText(this, themeName + " theme applied", Toast.LENGTH_SHORT).show();
            
            // Recreate activity to apply theme changes
            recreate();
        });

        autoPlaySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            savePreference("auto_play_enabled", isChecked);
            Toast.makeText(this, "Auto-play " + (isChecked ? "enabled" : "disabled"), Toast.LENGTH_SHORT).show();
        });

        // About section click handling for developer mode
        aboutSection.setOnClickListener(v -> {
            aboutClickCount++;
            if (aboutClickCount == 5) {
                showNotification();
                Toast.makeText(this, "🔧 Developer Mode: Notification triggered!", Toast.LENGTH_SHORT).show();
            } else if (aboutClickCount == 6) {
                Toast.makeText(this, "🎬 You found the secret! PopFlix Developer Mode activated!", Toast.LENGTH_LONG).show();
                showDeveloperOptions();
                aboutClickCount = 0; // Reset counter
            }
        });

        // Delete account button
        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
    }

    private void savePreference(String key, boolean value) {
        SharedPreferences preferences = getSharedPreferences("PopFlixSettings", MODE_PRIVATE);
        preferences.edit().putBoolean(key, value).apply();
    }

    private void saveThemePreference(int themeMode) {
        SharedPreferences preferences = getSharedPreferences("PopFlixSettings", MODE_PRIVATE);
        preferences.edit().putInt("theme_mode", themeMode).apply();
    }

    private void showNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        String channelId = "popflix_channel";

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                channelId,
                "PopFlix Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("PopFlix app notifications");
            notificationManager.createNotificationChannel(channel);
        }

        // Build and show notification with proper app icon
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Use app launcher icon
            .setContentTitle("PopFlix Developer Mode")
            .setContentText("🎬 You clicked the about section 5 times! One more for a surprise...")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);

        notificationManager.notify(1, builder.build());

        // Also show a toast to confirm notification was sent
        Toast.makeText(this, "📱 Notification sent! Check your notification bar.", Toast.LENGTH_SHORT).show();
    }

    private void showDeveloperOptions() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("🔧 Developer Mode Activated")
            .setMessage("Choose a developer option:")
            .setPositiveButton("Trigger Notification", (dialog, which) -> {
                showNotification();
                Toast.makeText(this, "📱 Notification triggered!", Toast.LENGTH_SHORT).show();
            })
            .setNeutralButton("Show Toast", (dialog, which) -> {
                Toast.makeText(this, "🍞 Developer Toast: All systems operational!", Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Clear Cache", (dialog, which) -> {
                Toast.makeText(this, "🧹 Cache cleared (simulated)", Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    private void showDeleteAccountDialog() {
        new MaterialAlertDialogBuilder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone and all your data will be permanently removed.")
            .setPositiveButton("Delete", (dialog, which) -> {
                // Here you would implement actual account deletion
                Toast.makeText(this, "Account deletion feature will be implemented soon", Toast.LENGTH_LONG).show();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
