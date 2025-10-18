package com.example.popflix.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.Log;
import android.view.MotionEvent;

import java.io.IOException;
import java.io.InputStream;

/**
 * Optimized image manipulation utility for PopFlix
 * Based on the provided image cropping code but optimized for modern Android
 */
public class ImageManipulationUtil {
    private static final String TAG = "ImageManipulationUtil";
    
    // Touch modes
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    
    // Matrix operations
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    
    // Touch state
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;
    private float[] lastEvent = null;
    
    /**
     * Optimized bitmap loading with proper memory management
     */
    public static Bitmap loadOptimizedBitmap(Context context, Uri uri, int reqWidth, int reqHeight) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            
            // First decode with inJustDecodeBounds=true to check dimensions
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565; // Use less memory
            
            // Reopen input stream for actual decoding
            inputStream.close();
            inputStream = context.getContentResolver().openInputStream(uri);
            
            return BitmapFactory.decodeStream(inputStream, null, options);
            
        } catch (IOException e) {
            Log.e(TAG, "Error loading bitmap: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculate optimal sample size for memory efficiency
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }
    
    /**
     * Create circular bitmap for profile pictures
     */
    public static Bitmap createCircularBitmap(Bitmap bitmap) {
        if (bitmap == null) return null;
        
        int size = Math.min(bitmap.getWidth(), bitmap.getHeight());
        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        paint.setDither(true);
        
        Rect rect = new Rect(0, 0, size, size);
        RectF rectF = new RectF(rect);
        
        canvas.drawOval(rectF, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        
        // Center the bitmap
        int x = (size - bitmap.getWidth()) / 2;
        int y = (size - bitmap.getHeight()) / 2;
        canvas.drawBitmap(bitmap, x, y, paint);
        
        return output;
    }
    
    /**
     * Handle touch events for image manipulation (optimized version)
     */
    public boolean handleTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                lastEvent = null;
                break;
                
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
                
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }
        return true;
    }
    
    /**
     * Calculate distance between two touch points (optimized)
     */
    private float spacing(MotionEvent event) {
        if (event.getPointerCount() < 2) return 0;
        
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y); // Using Math.sqrt instead of deprecated FloatMath
    }
    
    /**
     * Calculate midpoint between two touch points
     */
    private void midPoint(PointF point, MotionEvent event) {
        if (event.getPointerCount() < 2) return;
        
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }
    
    /**
     * Crop bitmap to specified dimensions
     */
    public static Bitmap cropBitmap(Bitmap source, int x, int y, int width, int height) {
        if (source == null) return null;
        
        // Ensure crop dimensions are within bounds
        x = Math.max(0, Math.min(x, source.getWidth() - 1));
        y = Math.max(0, Math.min(y, source.getHeight() - 1));
        width = Math.min(width, source.getWidth() - x);
        height = Math.min(height, source.getHeight() - y);
        
        if (width <= 0 || height <= 0) return null;
        
        return Bitmap.createBitmap(source, x, y, width, height);
    }
    
    /**
     * Resize bitmap maintaining aspect ratio
     */
    public static Bitmap resizeBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        if (bitmap == null) return null;
        
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        
        float scaleWidth = ((float) maxWidth) / width;
        float scaleHeight = ((float) maxHeight) / height;
        float scale = Math.min(scaleWidth, scaleHeight);
        
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }
    
    public Matrix getMatrix() {
        return matrix;
    }
    
    public void resetMatrix() {
        matrix.reset();
    }
}
