package com.example.popflix;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import java.util.Random;

public class NotificationSchedulingService extends Service {

    private static final String TAG = "NotificationSchedulingService";
    private static final long MIN_INTERVAL = 30 * 60 * 1000L; // 30 minutes
    private static final long MAX_INTERVAL = 2 * 60 * 60 * 1000L; // 2 hours

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "NotificationSchedulingService started");
        scheduleNextNotification(this);
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void scheduleNextNotification(Context context) {
        Log.d(TAG, "Scheduling next notification");
        if (!isDeveloperMode()) {
            Log.d(TAG, "Not in developer mode, skipping notification scheduling");
            return; // Only schedule in developer mode
        }

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Generate random interval between MIN_INTERVAL and MAX_INTERVAL
        Random random = new Random();
        long randomInterval = MIN_INTERVAL + (long) (random.nextDouble() * (MAX_INTERVAL - MIN_INTERVAL));
        long triggerTime = SystemClock.elapsedRealtime() + randomInterval;

        Log.d(TAG, "Next notification scheduled in " + (randomInterval / 1000) + " seconds");

        if (alarmManager != null) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerTime, pendingIntent);
        } else {
            Log.e(TAG, "AlarmManager is null");
        }
    }

    private static boolean isDeveloperMode() {
        // Check if the app is in debug mode
        boolean isDebug = BuildConfig.DEBUG;
        Log.d(TAG, "Developer mode: " + isDebug);
        return isDebug;
    }
}
