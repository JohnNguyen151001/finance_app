package com.financeapp.mobile.ui.wallet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeapp.mobile.databinding.FragmentAddWalletBinding;

public class AddWalletFragment extends Fragment {

    private FragmentAddWalletBinding binding;
    private AddWalletViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddWalletViewModel.class);

        binding.toolbarAddWallet.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Setup Spinner
        String[] walletTypes = {"CASH", "BANK", "CREDIT", "GOAL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, walletTypes);
        binding.spinnerWalletType.setAdapter(adapter);

        binding.btnSaveWallet.setOnClickListener(v -> {
            String name = binding.inputWalletName.getText().toString().trim();
            String type = binding.spinnerWalletType.getSelectedItem().toString();
            String balanceStr = binding.inputWalletBalance.getText().toString().trim();
            double balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);

            viewModel.saveWallet(name, type, balance);
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), "Wallet created successfully", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
