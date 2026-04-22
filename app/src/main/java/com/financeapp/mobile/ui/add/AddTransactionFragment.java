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

        // Đảm bảo ngày mặc định là hiện tại
        selectedDateMs = System.currentTimeMillis();
        binding.textDate.setText(DateDisplayUtils.formatFullDate(selectedDateMs));
        binding.textDate.setOnClickListener(v -> pickDate());

        binding.btnCancel.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.tabType.addOnTabSelectedListener(new com.google.android.material.tabs.TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(com.google.android.material.tabs.TabLayout.Tab tab) {
                viewModel.setTab(tab.getPosition());
            }
            @Override
            public void onTabUnselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
            @Override
            public void onTabReselected(com.google.android.material.tabs.TabLayout.Tab tab) {}
        });

        binding.inputAmount.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
                Toast.makeText(requireContext(), "Đã lưu giao dịch", Toast.LENGTH_SHORT).show();
                // Pop back to main and the onResume in HomeFragment will trigger refresh
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
        if (binding == null || meta == null || !isAdded()) return;

        // Cập nhật Wallet
        walletBuffer.clear();
        walletBuffer.addAll(meta.wallets);
        List<String> wNames = new ArrayList<>();
        if (walletBuffer.isEmpty()) {
            wNames.add("Chưa có ví (Vui lòng tạo ví)");
        } else {
            for (WalletEntity w : walletBuffer) {
                wNames.add(w.name + " (" + MoneyUtils.formatShortMillion(w.balance) + ")");
            }
        }
        ArrayAdapter<String> wa = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, wNames);
        binding.spinnerWallet.setAdapter(wa);
        if (!walletBuffer.isEmpty()) binding.spinnerWallet.setSelection(0);

        // Cập nhật Category
        categoryBuffer.clear();
        categoryBuffer.addAll(meta.categories);
        List<String> cNames = new ArrayList<>();
        if (categoryBuffer.isEmpty()) {
            cNames.add("Không tìm thấy hạng mục");
        } else {
            for (CategoryEntity c : categoryBuffer) {
                cNames.add(c.iconName != null ? (c.iconName + "  " + c.name) : c.name);
            }
        }
        ArrayAdapter<String> ca = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, cNames);
        binding.spinnerCategory.setAdapter(ca);
        if (!categoryBuffer.isEmpty()) binding.spinnerCategory.setSelection(0);
    }

    private void submit() {
        if (walletBuffer.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng tạo ví trước khi thêm giao dịch", Toast.LENGTH_SHORT).show();
            return;
        }
        if (categoryBuffer.isEmpty()) {
            Toast.makeText(requireContext(), "Vui lòng chọn hạng mục", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int wi = binding.spinnerWallet.getSelectedItemPosition();
        int ci = binding.spinnerCategory.getSelectedItemPosition();
        
        if (wi < 0 || ci < 0) {
            Toast.makeText(requireContext(), "Vui lòng chọn ví và hạng mục", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = parseAmount(binding.inputAmount.getText() != null
                ? binding.inputAmount.getText().toString() : "");
        if (amount <= 0) {
            Toast.makeText(requireContext(), "Vui lòng nhập số tiền hợp lệ", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.save(
                walletBuffer.get(wi).id,
                categoryBuffer.get(ci).id,
                amount,
                binding.inputNote.getText().toString(),
                selectedDateMs
        );
    }

    private static double parseAmount(String raw) {
        if (raw == null) return 0;
        String s = raw.replaceAll("[^0-9]", "");
        if (s.isEmpty()) return 0;
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.reloadMeta();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
