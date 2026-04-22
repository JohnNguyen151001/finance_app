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
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.R;
import com.financeapp.mobile.data.bootstrap.DatabaseSeeder;
import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.entity.UserEntity;
import com.financeapp.mobile.databinding.ActivityLoginBinding;
import com.financeapp.mobile.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.Executor;

/**
 * Đăng nhập bằng Firebase Email/Password + Biometric khi đã có session.
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
        // Nếu đã đăng nhập và đã xác thực email → thử biometric trước
        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isEmailVerified()) {
            syncUserToLocalAndSeed(mAuth.getCurrentUser());
            BiometricManager biometricManager = BiometricManager.from(this);
            if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    == BiometricManager.BIOMETRIC_SUCCESS) {
                showBiometricPrompt();
            } else {
                goToMain();
            }
        } else if (mAuth.getCurrentUser() != null && !mAuth.getCurrentUser().isEmailVerified()) {
            Toast.makeText(this, R.string.auth_verify_email, Toast.LENGTH_LONG).show();
            mAuth.signOut();
        }
    }

    private void syncUserToLocalAndSeed(FirebaseUser firebaseUser) {
        if (firebaseUser == null) return;
        
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getApplicationContext());
            UserEntity user = new UserEntity();
            user.uuid = firebaseUser.getUid();
            user.email = firebaseUser.getEmail();
            user.display_name = firebaseUser.getDisplayName();
            user.created_at = System.currentTimeMillis();
            db.userDao().insert(user);
            
            // Nếu là user mục tiêu và chưa có ví, hãy seed dữ liệu ngay
            if ("tc09042004@gmail.com".equals(user.email)) {
                if (db.walletDao().getAllForUser(user.uuid).isEmpty()) {
                    DatabaseSeeder.seedDataForUser(db, user.uuid);
                }
            }
        });
    }

    // ─── Biometric ───────────────────────────────────────────────────────────

    private void showBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                    }

                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        goToMain();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(LoginActivity.this, R.string.auth_biometric_failed, Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(getString(R.string.auth_biometric_title))
                .setSubtitle(getString(R.string.auth_biometric_subtitle))
                .setNegativeButtonText(getString(R.string.auth_use_password))
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    // ─── Login ───────────────────────────────────────────────────────────────

    private void attemptLogin() {
        String email = binding.emailInput.getText() != null
                ? binding.emailInput.getText().toString().trim() : "";
        String pass = binding.passwordInput.getText() != null
                ? binding.passwordInput.getText().toString().trim() : "";

        if (email.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, R.string.login_fill_both, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnLogin.setEnabled(false);
        mAuth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    binding.btnLogin.setEnabled(true);
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                            syncUserToLocalAndSeed(firebaseUser);
                            goToMain();
                        } else {
                            Toast.makeText(this, R.string.auth_verify_email, Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_login_failed);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ─── Forgot password ─────────────────────────────────────────────────────

    private void handleForgotPassword() {
        String email = binding.emailInput.getText() != null
                ? binding.emailInput.getText().toString().trim() : "";
        if (email.isEmpty()) {
            Toast.makeText(this, R.string.auth_enter_email_first, Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.auth_reset_sent, Toast.LENGTH_SHORT).show();
                    } else {
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.auth_reset_failed);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ─── Register link ───────────────────────────────────────────────────────

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

    // ─── Navigation ──────────────────────────────────────────────────────────

    private void goToMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
