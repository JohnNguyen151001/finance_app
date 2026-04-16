package com.financeapp.mobile.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.ActivityRegisterBinding;
import com.financeapp.mobile.ui.main.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.btnRegister.setOnClickListener(v -> attemptRegister());
        binding.backToLogin.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        String name = binding.nameInput.getText() != null ? binding.nameInput.getText().toString().trim() : "";
        String ageStr = binding.ageInput.getText() != null ? binding.ageInput.getText().toString().trim() : "";
        String phone = binding.phoneInput.getText() != null ? binding.phoneInput.getText().toString().trim() : "";
        String email = binding.emailInput.getText() != null ? binding.emailInput.getText().toString().trim() : "";
        String pass = binding.passwordInput.getText() != null ? binding.passwordInput.getText().toString().trim() : "";
        String confirmPass = binding.confirmPasswordInput.getText() != null ? binding.confirmPasswordInput.getText().toString().trim() : "";

        if (name.isEmpty() || ageStr.isEmpty() || phone.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age <= 0 || age > 120) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirmPass)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (pass.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful() && mAuth.getCurrentUser() != null) {
                        String userId = mAuth.getCurrentUser().getUid();
                        
                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("name", name);
                        userMap.put("age", age);
                        userMap.put("phone", phone);
                        userMap.put("email", email);

                        db.collection("users").document(userId)
                                .set(userMap)
                                .addOnSuccessListener(aVoid -> {
                                    sendVerificationEmail();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(RegisterActivity.this, "Failed to save user data: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(RegisterActivity.this, "Registration Failed: " + (task.getException() != null ? task.getException().getMessage() : "Unknown error"), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void sendVerificationEmail() {
        if (mAuth.getCurrentUser() != null) {
            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this,
                                    "Registration Successful! Please check your email for a verification link.",
                                    Toast.LENGTH_LONG).show();
                            
                            // Log out to force them to sign in after verification
                            mAuth.signOut();
                            
                            // Send back to Login
                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
