package com.example.popflix.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.example.popflix.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Firebase User Management Utility
 * Handles user data, profile pictures, and Firebase users table integration
 */
public class FirebaseUserManager {
    
    private static final String TAG = "FirebaseUserManager";
    private static final String USERS_COLLECTION = "users";
    
    private static FirebaseUserManager instance;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    
    private FirebaseUserManager() {
        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }
    
    public static FirebaseUserManager getInstance() {
        if (instance == null) {
            instance = new FirebaseUserManager();
        }
        return instance;
    }
    
    /**
     * Create or update user in Firebase users table
     */
    public void createOrUpdateUser(FirebaseUser user, UserUpdateCallback callback) {
        if (user == null) {
            if (callback != null) callback.onError("User is null");
            return;
        }
        
        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", user.getUid());
        userData.put("email", user.getEmail());
        userData.put("displayName", user.getDisplayName());
        userData.put("photoUrl", user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
        userData.put("lastLogin", System.currentTimeMillis());
        userData.put("provider", getAuthProvider(user));
        
        firestore.collection(USERS_COLLECTION)
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User data saved successfully");
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error saving user data", e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
    
    /**
     * Get user data from Firebase users table
     */
    public void getUserData(String uid, UserDataCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserData userData = new UserData();
                        userData.uid = documentSnapshot.getString("uid");
                        userData.email = documentSnapshot.getString("email");
                        userData.displayName = documentSnapshot.getString("displayName");
                        userData.photoUrl = documentSnapshot.getString("photoUrl");
                        userData.lastLogin = documentSnapshot.getLong("lastLogin");
                        userData.provider = documentSnapshot.getString("provider");
                        
                        if (callback != null) callback.onSuccess(userData);
                    } else {
                        if (callback != null) callback.onError("User not found");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user data", e);
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
    
    /**
     * Load profile picture with fallback logic
     */
    public void loadProfilePicture(Context context, ImageView imageView, int size) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            // Set default profile picture
            imageView.setImageResource(R.drawable.ic_profile);
            return;
        }

        // First try to get from Firebase users table
        getUserData(currentUser.getUid(), new UserDataCallback() {
            @Override
            public void onSuccess(UserData userData) {
                String photoUrl = userData.photoUrl;
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    loadImageWithGlide(context, photoUrl, imageView, size);
                } else {
                    // Try Firebase Auth photo URL
                    loadFromFirebaseAuth(context, currentUser, imageView, size);
                }
            }

            @Override
            public void onError(String error) {
                // Try Firebase Auth photo URL as fallback
                loadFromFirebaseAuth(context, currentUser, imageView, size);
            }
        });
    }
    
    private void loadFromFirebaseAuth(Context context, FirebaseUser user, ImageView imageView, int size) {
        if (user.getPhotoUrl() != null) {
            loadImageWithGlide(context, user.getPhotoUrl().toString(), imageView, size);
        } else {
            // Set default profile picture
            imageView.setImageResource(R.drawable.ic_profile);
        }
    }
    
    private void loadImageWithGlide(Context context, String photoUrl, ImageView imageView, int size) {
        Glide.with(context)
                .load(photoUrl)
                .apply(new RequestOptions()
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .override(size, size)
                        .centerCrop()
                        .circleCrop()) // Make it circular
                .into(imageView);
    }
    
    /**
     * Update user display name
     */
    public void updateDisplayName(String uid, String displayName, UserUpdateCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("displayName", displayName)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
    
    /**
     * Update user photo URL
     */
    public void updatePhotoUrl(String uid, String photoUrl, UserUpdateCallback callback) {
        firestore.collection(USERS_COLLECTION)
                .document(uid)
                .update("photoUrl", photoUrl)
                .addOnSuccessListener(aVoid -> {
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
    }
    
    private String getAuthProvider(FirebaseUser user) {
        if (user.getProviderData().size() > 1) {
            return user.getProviderData().get(1).getProviderId();
        }
        return "email";
    }
    
    // Data classes and interfaces
    public static class UserData {
        public String uid;
        public String email;
        public String displayName;
        public String photoUrl;
        public Long lastLogin;
        public String provider;
    }
    
    public interface UserDataCallback {
        void onSuccess(UserData userData);
        void onError(String error);
    }
    
    public interface UserUpdateCallback {
        void onSuccess();
        void onError(String error);
    }
}
