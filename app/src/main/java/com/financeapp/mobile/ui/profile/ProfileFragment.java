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
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || binding == null) return;

        if (user.getEmail() != null) {
            binding.textProfileEmail.setText(user.getEmail());
        }
        if (user.getDisplayName() != null && !user.getDisplayName().isEmpty()) {
            binding.textProfileName.setText(user.getDisplayName());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
