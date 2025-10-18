package com.example.popflix.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

/**
 * Performance optimization utilities for PopFlix app
 */
public class PerformanceOptimizer {
    
    /**
     * Optimize RecyclerView for better scrolling performance
     */
    public static void optimizeRecyclerView(RecyclerView recyclerView) {
        // Enable item animator optimizations
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(android.view.View.DRAWING_CACHE_QUALITY_HIGH);
        
        // Optimize nested scrolling
        recyclerView.setNestedScrollingEnabled(false);
    }
    
    /**
     * Load images with optimal Glide settings for movie posters
     */
    public static void loadMoviePoster(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        Glide.with(context)
            .load(imageUrl)
            .apply(new RequestOptions()
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, 450) // Standard movie poster ratio
                .centerCrop())
            .into(imageView);
    }
    
    /**
     * Load images with optimal settings for backdrop images
     */
    public static void loadBackdropImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        Glide.with(context)
            .load(imageUrl)
            .apply(new RequestOptions()
                .placeholder(android.R.color.darker_gray)
                .error(android.R.color.darker_gray)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(800, 450) // Standard backdrop ratio
                .centerCrop())
            .into(imageView);
    }
    
    /**
     * Preload images for better user experience
     */
    public static void preloadImage(Context context, String imageUrl) {
        if (context == null || imageUrl == null) return;
        
        Glide.with(context)
            .load(imageUrl)
            .apply(new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(300, 450))
            .preload();
    }
    
    /**
     * Clear Glide cache to free memory when needed
     */
    public static void clearImageCache(Context context) {
        if (context == null) return;
        
        // Clear memory cache on main thread
        Glide.get(context).clearMemory();
        
        // Clear disk cache on background thread
        new Thread(() -> Glide.get(context).clearDiskCache()).start();
    }
    
    /**
     * Load profile image with circular transformation
     */
    public static void loadProfileImage(Context context, String imageUrl, ImageView imageView) {
        if (context == null || imageView == null) return;
        
        Glide.with(context)
            .asBitmap()
            .load(imageUrl)
            .apply(new RequestOptions()
                .placeholder(com.example.popflix.R.drawable.ic_profile)
                .error(com.example.popflix.R.drawable.ic_profile)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(200, 200)
                .centerCrop())
            .into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                    // Create circular bitmap
                    Bitmap circularBitmap = ImageManipulationUtil.createCircularBitmap(resource);
                    imageView.setImageBitmap(circularBitmap);
                }
                
                @Override
                public void onLoadCleared(Drawable placeholder) {
                    imageView.setImageDrawable(placeholder);
                }
            });
    }
    
    /**
     * Optimize memory usage by recycling bitmaps
     */
    public static void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
        }
    }
    
    /**
     * Check if we're running low on memory
     */
    public static boolean isLowMemory(Context context) {
        android.app.ActivityManager activityManager = 
            (android.app.ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        android.app.ActivityManager.MemoryInfo memoryInfo = 
            new android.app.ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        
        return memoryInfo.lowMemory;
    }
    
    /**
     * Get optimal image quality based on available memory
     */
    public static int getOptimalImageQuality(Context context) {
        if (isLowMemory(context)) {
            return 60; // Lower quality for low memory devices
        } else {
            return 80; // Higher quality for devices with sufficient memory
        }
    }
}
