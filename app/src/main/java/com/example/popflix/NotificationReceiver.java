package com.example.popflix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "NotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "NotificationReceiver triggered");

        // Show a debug toast to confirm receiver is working
        NotificationUtils.showToast(context, "Notification triggered!");

        // Show the custom notification
        NotificationUtils.showCustomNotification(context, "PopFlix Reminder", "Time for some movie magic!");

        // Schedule the next random notification
        NotificationSchedulingService.scheduleNextNotification(context);
    }
}
