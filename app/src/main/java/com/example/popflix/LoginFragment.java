package com.example.popflix;

import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.popflix.databinding.FragmentLoginBinding;
import com.example.popflix.home.HomeActivity;
import com.example.popflix.NotificationUtils;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.example.popflix.utils.FirebaseUserManager;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFragment extends Fragment {

    // ==================== PROPERTIES AND CONSTANTS ====================
    // Fragment binding for view access
    private FragmentLoginBinding binding;

    // Firebase authentication instance
    private FirebaseAuth mAuth;

    // Firebase User Manager for consistent data handling
    private FirebaseUserManager userManager;

    // Counter for secret anonymous login trigger
    private int secretClickCount = 0;

    // Google Sign-In client for OAuth authentication
    private GoogleSignInClient googleSignInClient;

    // Request code for Google Sign-In activity result
    private static final int RC_SIGN_IN = 9001;

    // Firestore database instance for user data storage
    private FirebaseFirestore db;

    // ==================== FRAGMENT LIFECYCLE METHODS ====================

    /**
     * Creates and returns the view hierarchy associated with the fragment.
     * Initializes Firebase Auth and User Manager instances.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        userManager = FirebaseUserManager.getInstance();
        return binding.getRoot();
    }

    /**
     * Called immediately after onCreateView() when the fragment's view is created.
     * Sets up Google Sign-In configuration and button click listeners.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ==================== INITIALIZATION ====================
        // Configure Google Sign-In options and client
        setupGoogleSignIn();

        // Set up click listeners for login actions
        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.googleSigninButton.setOnClickListener(v -> signInWithGoogle());
        binding.switchToSignupButton.setOnClickListener(v -> ((AuthActivity) requireActivity()).loadFragment(new SignupFragment()));

        // ==================== SECRET FEATURE ====================
        // Hidden anonymous login triggered by clicking title 5 times
        binding.loginTitle.setOnClickListener(v -> {
            secretClickCount++;
            if (secretClickCount == 5) {
                secretClickCount = 0;
                signInAnonymously();
            }
        });
    }

    // ==================== GOOGLE SIGN-IN METHODS ====================

    /**
     * Configures Google Sign-In options with OAuth token request and email.
     * Creates and assigns the GoogleSignInClient instance.
     */
    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    /**
     * Initiates Google Sign-In process by launching the sign-in intent.
     * Handles the authentication flow through activity result.
     */
    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Processes the result from Google Sign-In activity.
     * Extracts account information and proceeds with Firebase authentication.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    /**
     * Handles the result of Google Sign-In attempt.
     * Processes successful authentication or displays error message on failure.
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            firebaseAuthWithGoogle(account);
        } catch (ApiException e) {
            NotificationUtils.showToast(getContext(), "Google sign in failed: " + e.getMessage());
        }
    }

    /**
     * Authenticates user with Firebase using Google account credentials.
     * Downloads profile picture, saves user data using FirebaseUserManager, and navigates to home screen on success.
     */
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        binding.progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithCredential(com.google.firebase.auth.GoogleAuthProvider.getCredential(acct.getIdToken(), null))
                .addOnCompleteListener(requireActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        String username = acct.getDisplayName() != null ? acct.getDisplayName() : "Google User";

                        // Save username to SharedPreferences for offline access
                        SharedPreferences preferences = requireActivity().getSharedPreferences("PopFlixPrefs", requireActivity().MODE_PRIVATE);
                        preferences.edit().putString("user_display_name", username).apply();

                        // Download and save Google profile picture to Firestore
                        if (acct.getPhotoUrl() != null) {
                            downloadAndSaveProfilePicture(acct.getPhotoUrl().toString(), userId);
                        }

                        // Update user data using FirebaseUserManager for consistency
                        userManager.createOrUpdateUser(mAuth.getCurrentUser(), new FirebaseUserManager.UserUpdateCallback() {
                            @Override
                            public void onSuccess() {
                                // Also save additional profile data specific to this app
                                saveAdditionalUserData(userId, username, acct.getEmail(), true);
                            }

                            @Override
                            public void onError(String error) {
                                Log.e("LoginFragment", "Error updating user data: " + error);
                                // Still try to save additional data even if user manager fails
                                saveAdditionalUserData(userId, username, acct.getEmail(), true);
                            }
                        });

                        NotificationUtils.showToast(getContext(), "Google sign in successful!");
                        navigateToHome();
                    } else {
                        NotificationUtils.showToast(getContext(), "Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    // ==================== USER DATA MANAGEMENT ====================

    /**
     * Saves additional PopFlix-specific user data to Firestore.
     * This complements the FirebaseUserManager data with app-specific fields.
     */
    private void saveAdditionalUserData(String userId, String username, String email, boolean isGoogleSignIn) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("displayName", username);
        user.put("googleSignIn", isGoogleSignIn);
        user.put("createdAt", System.currentTimeMillis());

        // Use direct Firestore access for app-specific data
        FirebaseFirestore.getInstance().collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("LoginFragment", "Additional user data saved successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("LoginFragment", "Error saving additional user data: " + e.getMessage());
                });
    }

    // ==================== PROFILE PICTURE HANDLING ====================

    /**
     * Downloads Google profile picture in background thread and saves to Firestore.
     * Compresses image to Base64 format for storage efficiency.
     */
    private void downloadAndSaveProfilePicture(String photoUrl, String userId) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            try {
                URL url = new URL(photoUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(input);
                
                if (bitmap != null) {
                    // Compress and encode to Base64
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    byte[] imageBytes = baos.toByteArray();
                    String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                    
                    // Save to Firestore
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("profilePicture", encodedImage);
                    
                    db.collection("users").document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                // Also save to SharedPreferences for offline access
                                SharedPreferences preferences = requireActivity().getSharedPreferences("PopFlixPrefs", requireActivity().MODE_PRIVATE);
                                preferences.edit().putString("custom_profile_pic", encodedImage).apply();
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ==================== EMAIL/PASSWORD AUTHENTICATION ====================

    /**
     * Authenticates user with email and password credentials.
     * Validates input fields and handles Firebase authentication.
     */
    private void loginUser() {
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            NotificationUtils.showToast(getContext(), "Please fill in all fields");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        NotificationUtils.showToast(getContext(), "Login Successful.");
                        navigateToHome();
                    } else {
                        NotificationUtils.showToast(getContext(), "Authentication failed: " + task.getException().getMessage());
                    }
                });
    }

    // ==================== ANONYMOUS AUTHENTICATION ====================

    /**
     * Signs in user anonymously for guest access.
     * Triggered by secret click combination on login title.
     */
    private void signInAnonymously() {
        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(requireActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        NotificationUtils.showToast(getContext(), "Signed in as Guest.");
                        navigateToHome();
                    } else {
                        NotificationUtils.showToast(getContext(), "Anonymous sign-in failed: " + task.getException().getMessage());
                    }
                });
    }

    // ==================== NAVIGATION METHODS ====================

    /**
     * Navigates user to home screen and finishes current activity.
     * Called after successful authentication of any type.
     */
    private void navigateToHome() {
        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    // ==================== LIFECYCLE CLEANUP ====================

    /**
     * Cleans up fragment view references to prevent memory leaks.
     * Called when fragment view is being destroyed.
     */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}