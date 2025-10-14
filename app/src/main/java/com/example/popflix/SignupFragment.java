package com.example.popflix;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.popflix.databinding.FragmentSignupBinding;
import com.example.popflix.home.HomeActivity;
import com.example.popflix.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class SignupFragment extends Fragment {

    private FragmentSignupBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private int secretClickCount = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.signupButton.setOnClickListener(v -> registerUser());
        binding.switchToLoginButton.setOnClickListener(v -> ((AuthActivity) requireActivity()).loadFragment(new LoginFragment()));

        // Secret anonymous login trigger
        binding.signupTitle.setOnClickListener(v -> {
            secretClickCount++;
            if (secretClickCount == 5) {
                secretClickCount = 0;
                signInAnonymously();
            }
        });
    }

    private void registerUser() {
        String username = binding.usernameEditText.getText().toString().trim();
        String email = binding.emailEditText.getText().toString().trim();
        String password = binding.passwordEditText.getText().toString().trim();
        String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            NotificationUtils.showToast(getContext(), "Please fill in all fields");
            return;
        }

        if (!password.equals(confirmPassword)) {
            NotificationUtils.showToast(getContext(), "Passwords do not match");
            return;
        }

        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        String userId = mAuth.getCurrentUser().getUid();
                        // Store additional user info in Firestore
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", username);
                        user.put("email", email);

                        db.collection("users").document(userId)
                                .set(user)
                                .addOnSuccessListener(aVoid -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    NotificationUtils.showToast(getContext(), "Registration Successful.");
                                    navigateToHome();
                                })
                                .addOnFailureListener(e -> {
                                    binding.progressBar.setVisibility(View.GONE);
                                    NotificationUtils.showToast(getContext(), "Error saving user data: " + e.getMessage());
                                });
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                        NotificationUtils.showToast(getContext(), "Registration failed: " + task.getException().getMessage());
                    }
                });
    }

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

    private void navigateToHome() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}