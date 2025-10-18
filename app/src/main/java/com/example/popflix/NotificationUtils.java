package com.example.popflix;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {

    private static final String CHANNEL_ID = "popflix_notifications";
    private static final int NOTIFICATION_ID = 1001;

    public static void showToast(Context context, String message) {
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

    public static void showToast(Context context, String message, int duration) {
        if (context != null) {
            Toast.makeText(context, message, duration).show();
        }
    }

    public static void showCustomNotification(Context context, String title, String message) {
        Log.d("NotificationUtils", "Creating custom notification: " + title);
        createNotificationChannel(context);

        // Create an intent to open the main activity when notification is tapped
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Build the notification with custom styling and heads-up behavior
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for heads-up notification
                .setCategory(NotificationCompat.CATEGORY_REMINDER) // Category for reminder-style pop-up
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setColor(Color.parseColor("#FF5722"))
                .setVibrate(new long[]{0, 250, 250, 250})
                .setLights(Color.parseColor("#FF5722"), 1000, 1000)
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Includes sound, vibration, and lights

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Log.d("NotificationUtils", "Notification shown successfully");
        } catch (SecurityException e) {
            Log.e("NotificationUtils", "Failed to show notification: " + e.getMessage());
        }
    }

    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "PopFlix Notifications";
            String description = "Notifications for PopFlix reminders";
            int importance = NotificationManager.IMPORTANCE_HIGH; // High importance for heads-up
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            channel.enableVibration(true);
            channel.setLightColor(Color.parseColor("#FF5722"));
            channel.setVibrationPattern(new long[]{0, 250, 250, 250});
            channel.setShowBadge(true); // Show badge on app icon

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}