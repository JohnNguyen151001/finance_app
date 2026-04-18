package com.financeapp.mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.ActivityRegisterBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Màn đăng ký tài khoản mới.
 * Flow: Nhập thông tin → createUserWithEmailAndPassword → updateProfile (displayName) → gửi email xác thực → về Login.
 * Dữ liệu app (ví, giao dịch) chỉ lưu SQLite local; không dùng Firestore.
 */
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.backToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = getText(binding.nameInput);
        String ageStr = getText(binding.ageInput);
        String phone = getText(binding.phoneInput);
        String email = getText(binding.emailInput);
        String pass = getText(binding.passwordInput);
        String confirmPass = getText(binding.confirmPasswordInput);

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty()
                || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, R.string.register_fill_all, Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.register_invalid_age, Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, R.string.register_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 6) {
            Toast.makeText(this, R.string.register_password_too_short, Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnRegister.setEnabled(false);

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        // Tuổi / SĐT không lưu cloud (SQLite-only); có thể mở rộng bảng user local sau
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name)
                                .build();
                        mAuth.getCurrentUser().updateProfile(profileUpdates)
                                .addOnCompleteListener(profileTask -> {
                                    if (!profileTask.isSuccessful()) {
                                        binding.btnRegister.setEnabled(true);
                                        Toast.makeText(this,
                                                profileTask.getException() != null
                                                        ? profileTask.getException().getMessage()
                                                        : getString(R.string.register_failed),
                                                Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    sendVerificationEmail();
                                });
                    } else {
                        binding.btnRegister.setEnabled(true);
                        String msg = task.getException() != null
                                ? task.getException().getMessage()
                                : getString(R.string.register_failed);
                        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationEmail() {
        if (mAuth.getCurrentUser() == null) return;
        mAuth.getCurrentUser().sendEmailVerification()
                .addOnCompleteListener(task -> {
                    binding.btnRegister.setEnabled(true);
                    if (task.isSuccessful()) {
                        Toast.makeText(this, R.string.register_success_verify, Toast.LENGTH_LONG).show();
                        mAuth.signOut();
                        startActivity(new Intent(this, LoginActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                        finish();
                    } else {
                        Toast.makeText(this, R.string.register_verify_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getText(android.widget.EditText et) {
        return et != null && et.getText() != null ? et.getText().toString().trim() : "";
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
