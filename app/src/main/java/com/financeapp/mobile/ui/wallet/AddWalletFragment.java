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

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.FragmentAddWalletBinding;

public class AddWalletFragment extends Fragment {

    private FragmentAddWalletBinding binding;
    private AddWalletViewModel viewModel;
    private long walletId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddWalletBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddWalletViewModel.class);

        if (getArguments() != null) {
            walletId = getArguments().getLong("walletId", -1);
        }

        binding.toolbarAddWallet.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        if (walletId > 0) {
            binding.toolbarAddWallet.setTitle("Chỉnh sửa ví");
            viewModel.loadWallet(walletId);
        }

        // Spinner loại ví
        String[] walletTypes = {"CASH", "BANK", "CREDIT", "GOAL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_dropdown_item, walletTypes);
        binding.spinnerWalletType.setAdapter(adapter);

        viewModel.getExistingWallet().observe(getViewLifecycleOwner(), w -> {
            if (w != null) {
                binding.inputWalletName.setText(w.name);
                binding.inputWalletBalance.setText(String.valueOf(w.balance));
                for (int i = 0; i < walletTypes.length; i++) {
                    if (walletTypes[i].equals(w.type)) {
                        binding.spinnerWalletType.setSelection(i);
                        break;
                    }
                }
            }
        });

        binding.btnSaveWallet.setOnClickListener(v -> {
            String name = binding.inputWalletName.getText() != null
                    ? binding.inputWalletName.getText().toString().trim() : "";
            String type = binding.spinnerWalletType.getSelectedItem() != null
                    ? binding.spinnerWalletType.getSelectedItem().toString() : "CASH";
            String balanceStr = binding.inputWalletBalance.getText() != null
                    ? binding.inputWalletBalance.getText().toString().trim() : "";
            double balance = 0;
            try {
                balance = balanceStr.isEmpty() ? 0 : Double.parseDouble(balanceStr);
            } catch (NumberFormatException ignored) {}
            
            viewModel.saveWallet(walletId, name, type, balance);
        });

        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), success -> {
            if (Boolean.TRUE.equals(success)) {
                String msg = (walletId > 0) ? "Đã cập nhật ví" : "Đã tạo ví mới";
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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
