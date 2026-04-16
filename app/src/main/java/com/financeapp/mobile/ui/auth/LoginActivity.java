package com.financeapp.mobile.ui.auth;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.ActivityLoginBinding;
import com.financeapp.mobile.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

/**
 * Màn đăng nhập theo Figma (SpendSmart). Luồng demo: bất kỳ email/mật khẩu không rỗng đều vào app.
 */
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnLogin.setOnClickListener(v -> attemptLogin());
        binding.forgotPassword.setOnClickListener(v -> handleForgotPassword());
        setupRegisterLink();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            if (mAuth.getCurrentUser().isEmailVerified()) {
                androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
                if (biometricManager.canAuthenticate(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG) 
                        == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS) {
                    showBiometricPrompt();
                } else {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                }
            } else {
                Toast.makeText(this, "Please verify your email to continue", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
            }
        }
    }

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode,
                                              @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getApplicationContext(),
                        "Authentication error: " + errString, Toast.LENGTH_SHORT).show();
                // Stay on login screen to allow password fallback
            }

            @Override
            public void onAuthenticationSucceeded(
                    @NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getApplicationContext(),
                        "Authentication succeeded!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getApplicationContext(), "Authentication failed",
                        Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Biometric login for Finance App")
                .setSubtitle("Log in using your biometric credential")
                .setNegativeButtonText("Use account password")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void handleForgotPassword() {
        android.util.Log.d("AUTH_DEBUG", "Forgot Password button clicked!");
        CharSequence email = binding.emailInput.getText();
        if (email == null || email.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter your email first to reset password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Toast.makeText(this, "Processing reset request...", Toast.LENGTH_SHORT).show();
        
        mAuth.sendPasswordResetEmail(email.toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                        android.util.Log.d("AUTH_DEBUG", "Reset email sent successfully.");
                    } else {
                        Toast.makeText(this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        android.util.Log.e("AUTH_DEBUG", "Reset email failed", task.getException());
                    }
                });
    }

    private void setupRegisterLink() {
        String prefix = getString(R.string.login_register_prompt);
        String action = getString(R.string.login_register_action);
        SpannableString ss = new SpannableString(prefix + action);
        int start = prefix.length();
        int end = start + action.length();
        ss.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getColor(R.color.spend_green));
                ds.setUnderlineText(false);
                ds.setFakeBoldText(true);
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.registerPrompt.setText(ss);
        binding.registerPrompt.setMovementMethod(LinkMovementMethod.getInstance());
        binding.registerPrompt.setHighlightColor(Color.TRANSPARENT);
    }

    private void attemptLogin() {
        android.util.Log.d("AUTH_DEBUG", "Login button clicked!");
        CharSequence email = binding.emailInput.getText();
        CharSequence pass = binding.passwordInput.getText();
        if (email == null || email.toString().trim().isEmpty()
                || pass == null || pass.toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.login_fill_both, Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(this, "Calling Firebase Auth...", Toast.LENGTH_SHORT).show();

        mAuth.signInWithEmailAndPassword(email.toString().trim(), pass.toString().trim())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                            android.util.Log.d("AUTH_DEBUG", "Login successful");
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Please verify your email before logging in.", Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown"), Toast.LENGTH_LONG).show();
                        android.util.Log.e("AUTH_DEBUG", "Login failed", task.getException());
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
