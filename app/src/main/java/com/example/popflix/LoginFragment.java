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
import com.example.popflix.databinding.FragmentLoginBinding;
import com.example.popflix.home.HomeActivity;
import com.example.popflix.utils.NotificationUtils;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;
    private int secretClickCount = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.loginButton.setOnClickListener(v -> loginUser());
        binding.switchToSignupButton.setOnClickListener(v -> ((AuthActivity) requireActivity()).loadFragment(new SignupFragment()));

        // Secret anonymous login trigger
        binding.loginTitle.setOnClickListener(v -> {
            secretClickCount++;
            if (secretClickCount == 5) {
                secretClickCount = 0;
                signInAnonymously();
            }
        });
    }

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

    private void signInAnonymously() {
        binding.progressBar.setVisibility(View.VISIBLE);
        mAuth.signInAnonymously()
                .addOnCompleteListener(requireActivity(), task -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (task..isSuccessful()) {
                        NotificationUtils.showToast(getContext(), "Signed in as Guest.");
                        navigateToHome();
                    } else {
                        NotificationUtils.showToast(getContext(), "Anonymous sign-in failed: " + task.getException().getMessage());
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(requireActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}