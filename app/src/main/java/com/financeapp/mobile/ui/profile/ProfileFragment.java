package com.financeapp.mobile.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.FragmentProfileBinding;
import com.financeapp.mobile.ui.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.rowWallets.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_wallet));
        binding.rowCategories.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_category));
        binding.rowAlerts.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.profile_alerts, Toast.LENGTH_SHORT).show());
        binding.rowSettings.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.profile_settings, Toast.LENGTH_SHORT).show());
        binding.rowLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
        });

        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            String email = currentUser.getEmail();
            
            if (email != null) {
                binding.textProfileEmail.setText(email);
            }

            FirebaseFirestore.getInstance().collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            if (name != null) {
                                binding.textProfileName.setText(name);
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(requireContext(), "Failed to load user profile", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
