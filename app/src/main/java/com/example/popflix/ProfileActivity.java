package com.example.popflix;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.popflix.utils.FirebaseUserManager;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private SharedPreferences preferences;
    private FirebaseUser currentUser;
    private FirebaseUserManager userManager;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase user
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        preferences = getSharedPreferences("PopFlixPrefs", MODE_PRIVATE);
        userManager = FirebaseUserManager.getInstance();
        db = FirebaseFirestore.getInstance();

        setupToolbar();
        setupProfileInfo();
        setupButtons();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupProfileInfo() {
        ImageView profileImage = findViewById(R.id.profile_image);
        TextView profileName = findViewById(R.id.profile_name);
        TextView profileEmail = findViewById(R.id.profile_email);

        profileEmail.setText(currentUser.getEmail());

        // Make name clickable for editing
        profileName.setOnClickListener(v -> showEditNameDialog());

        // Load profile data from Firestore
        loadProfileFromFirestore(profileImage, profileName);
    }

    private void loadProfileFromFirestore(ImageView profileImage, TextView profileName) {
        db.collection("users").document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Get display name from Firestore
                        String displayName = documentSnapshot.getString("displayName");
                        if (displayName == null || displayName.isEmpty()) {
                            displayName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
                        }
                        profileName.setText(displayName);

                        // Get profile picture from Firestore
                        String profilePicBase64 = documentSnapshot.getString("profilePicture");
                        if (profilePicBase64 != null && !profilePicBase64.isEmpty()) {
                            try {
                                byte[] decodedBytes = Base64.decode(profilePicBase64, Base64.DEFAULT);
                                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                                profileImage.setImageBitmap(decodedBitmap);
                            } catch (Exception e) {
                                loadFirebaseAuthProfilePicture(profileImage);
                            }
                        } else {
                            loadFirebaseAuthProfilePicture(profileImage);
                        }
                    } else {
                        // Fallback to Firebase Auth data
                        String displayName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
                        profileName.setText(displayName);
                        loadFirebaseAuthProfilePicture(profileImage);
                    }
                })
                .addOnFailureListener(e -> {
                    // Fallback to Firebase Auth data
                    String displayName = currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "User";
                    profileName.setText(displayName);
                    loadFirebaseAuthProfilePicture(profileImage);
                });
    }

    private void loadFirebaseAuthProfilePicture(ImageView profileImage) {
        // Load profile picture from Firebase Auth if available
        if (currentUser.getPhotoUrl() != null) {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile)
                    .into(profileImage);
        } else {
            profileImage.setImageResource(R.drawable.ic_profile);
        }
    }

    private void setupButtons() {
        MaterialButton editProfileButton = findViewById(R.id.edit_profile_button);
        MaterialButton renameProfileButton = findViewById(R.id.rename_profile_button);
        MaterialButton changePasswordButton = findViewById(R.id.change_password_button);

        editProfileButton.setOnClickListener(v -> {
            selectProfilePicture();
        });

        renameProfileButton.setOnClickListener(v -> {
            showEditNameDialog();
        });

        changePasswordButton.setOnClickListener(v -> {
            // For now, show a message that this feature requires email verification
            Toast.makeText(this, "Password change requires email verification. Please check your email.", Toast.LENGTH_LONG).show();
        });
    }

    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Name");

        // Set up the input
        final EditText input = new EditText(this);
        input.setHint("Enter your name");
        
        // Get current name from display
        TextView profileName = findViewById(R.id.profile_name);
        input.setText(profileName.getText().toString());
        
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                // Save to Firestore using FirebaseUserManager for consistency
                userManager.updateDisplayName(currentUser.getUid(), newName, new FirebaseUserManager.UserUpdateCallback() {
                    @Override
                    public void onSuccess() {
                        // Update the display
                        profileName.setText(newName);

                        // Also save to SharedPreferences for offline access
                        preferences.edit().putString("user_display_name", newName).apply();

                        Toast.makeText(ProfileActivity.this, "Name updated successfully!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(ProfileActivity.this, "Error updating name: " + error, Toast.LENGTH_SHORT).show();
                    }
                });

                // Also update additional user data for app-specific fields
                Map<String, Object> updates = new HashMap<>();
                updates.put("displayName", newName);
                updates.put("username", newName);

                FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid())
                        .update(updates)
                        .addOnSuccessListener(aVoid -> {
                            Log.d("ProfileActivity", "Additional user data updated successfully");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("ProfileActivity", "Error updating additional user data: " + e.getMessage());
                        });
            } else {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void selectProfilePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                // Load bitmap with basic decoding
                Bitmap bitmap = null;
                try {
                    bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                } catch (Exception e) {
                    Log.e("ProfileActivity", "Error loading bitmap: " + e.getMessage());
                }

                if (bitmap != null) {
                    ImageView profileImage = findViewById(R.id.profile_image);
                    profileImage.setImageBitmap(bitmap);

                    // Save to preferences with compression
                    saveImageToPreferences(bitmap);

                    Toast.makeText(this, "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();

                    // Clean up bitmap to free memory
                    if (!bitmap.isRecycled()) {
                        bitmap.recycle();
                    }
                } else {
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Log.e("ProfileActivity", "Error handling image: " + e.getMessage());
                Toast.makeText(this, "Error processing image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToPreferences(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);

        // Save to Firestore
        Map<String, Object> updates = new HashMap<>();
        updates.put("profilePicture", encodedImage);
        
        db.collection("users").document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Also save to SharedPreferences for offline access
                    preferences.edit().putString("custom_profile_pic", encodedImage).apply();
                    Log.d("ProfileActivity", "Profile picture saved to Firestore successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error saving profile picture to Firestore", e);
                    // Still save to SharedPreferences as fallback
                    preferences.edit().putString("custom_profile_pic", encodedImage).apply();
                });
    }
}
