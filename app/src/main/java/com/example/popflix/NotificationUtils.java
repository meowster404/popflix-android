package com.example.popflix;

import android.content.Context;
import android.widget.Toast;

public class NotificationUtils {
    
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
}