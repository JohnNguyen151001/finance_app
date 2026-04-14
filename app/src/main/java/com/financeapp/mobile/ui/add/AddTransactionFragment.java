package com.financeapp.mobile.ui.add;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;
import com.financeapp.mobile.databinding.FragmentAddTransactionBinding;
import com.financeapp.mobile.ui.format.DateDisplayUtils;
import com.financeapp.mobile.ui.format.MoneyUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTransactionFragment extends Fragment {

    private FragmentAddTransactionBinding binding;
    private AddTransactionViewModel viewModel;
    private final List<WalletEntity> walletBuffer = new ArrayList<>();
    private final List<CategoryEntity> categoryBuffer = new ArrayList<>();
    private long selectedDateMs = System.currentTimeMillis();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(AddTransactionViewModel.class);

        binding.textDate.setText(DateDisplayUtils.formatFullDate(selectedDateMs));
        binding.textDate.setOnClickListener(v -> pickDate());

        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.tabType.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                viewModel.setTab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {
            }
        });

        binding.inputAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                double v = parseAmount(s != null ? s.toString() : "");
                binding.textAmountDisplay.setText(MoneyUtils.formatVnd(v));
            }
        });

        viewModel.getMeta().observe(getViewLifecycleOwner(), this::applyMeta);
        viewModel.getSaveDone().observe(getViewLifecycleOwner(), ok -> {
            if (Boolean.TRUE.equals(ok)) {
                viewModel.clearSaveDone();
                Navigation.findNavController(requireView()).popBackStack();
            }
        });

        binding.btnSave.setOnClickListener(v -> submit());
    }

    private void pickDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(selectedDateMs);
        int y = c.get(Calendar.YEAR);
        int m = c.get(Calendar.MONTH);
        int d = c.get(Calendar.DAY_OF_MONTH);
        new android.app.DatePickerDialog(requireContext(), (picker, year, month, dayOfMonth) -> {
            Calendar out = Calendar.getInstance();
            out.set(year, month, dayOfMonth, 12, 0, 0);
            out.set(Calendar.MILLISECOND, 0);
            selectedDateMs = out.getTimeInMillis();
            binding.textDate.setText(DateDisplayUtils.formatFullDate(selectedDateMs));
        }, y, m, d).show();
    }

    private void applyMeta(AddTransactionViewModel.FormMeta meta) {
        if (binding == null || meta == null) {
            return;
        }
        walletBuffer.clear();
        walletBuffer.addAll(meta.wallets);
        List<String> wNames = new ArrayList<>();
        for (WalletEntity w : walletBuffer) {
            wNames.add(w.name);
        }
        ArrayAdapter<String> wa = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, wNames);
        binding.spinnerWallet.setAdapter(wa);

        categoryBuffer.clear();
        categoryBuffer.addAll(meta.categories);
        List<String> cNames = new ArrayList<>();
        for (CategoryEntity c : categoryBuffer) {
            cNames.add(c.iconKey != null ? (c.iconKey + "  " + c.name) : c.name);
        }
        ArrayAdapter<String> ca = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, cNames);
        binding.spinnerCategory.setAdapter(ca);
    }

    private void submit() {
        if (walletBuffer.isEmpty() || categoryBuffer.isEmpty()) {
            Toast.makeText(requireContext(), R.string.add_wallet, Toast.LENGTH_SHORT).show();
            return;
        }
        int wi = binding.spinnerWallet.getSelectedItemPosition();
        int ci = binding.spinnerCategory.getSelectedItemPosition();
        if (wi == AdapterView.INVALID_POSITION || ci == AdapterView.INVALID_POSITION) {
            Toast.makeText(requireContext(), R.string.add_category, Toast.LENGTH_SHORT).show();
            return;
        }
        double amount = parseAmount(binding.inputAmount.getText() != null
                ? binding.inputAmount.getText().toString() : "");
        if (amount <= 0) {
            Toast.makeText(requireContext(), R.string.add_amount_hint, Toast.LENGTH_SHORT).show();
            return;
        }
        CharSequence note = binding.inputNote.getText();
        viewModel.save(
                walletBuffer.get(wi).id,
                categoryBuffer.get(ci).id,
                amount,
                note != null ? note.toString() : "",
                selectedDateMs
        );
    }

    private static double parseAmount(String raw) {
        if (raw == null) {
            return 0;
        }
        String s = raw.replace(",", ".").replaceAll("[^0-9.]", "");
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
