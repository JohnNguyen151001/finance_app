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

    private void handleForgotPassword() {
        CharSequence email = binding.emailInput.getText();
        if (email == null || email.toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter your email first to reset password", Toast.LENGTH_SHORT).show();
            return;
        }
        mAuth.sendPasswordResetEmail(email.toString().trim())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Failed to send reset email: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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
        CharSequence email = binding.emailInput.getText();
        CharSequence pass = binding.passwordInput.getText();
        if (email == null || email.toString().trim().isEmpty()
                || pass == null || pass.toString().trim().isEmpty()) {
            Toast.makeText(this, R.string.login_fill_both, Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email.toString().trim(), pass.toString().trim())
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this, "Login Successful.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Authentication Failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
