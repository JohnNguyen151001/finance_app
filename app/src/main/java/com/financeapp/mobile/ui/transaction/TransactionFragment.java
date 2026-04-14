package com.financeapp.mobile.ui.transaction;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.financeapp.mobile.databinding.FragmentTransactionBinding;

public class TransactionFragment extends Fragment {

    private FragmentTransactionBinding binding;
    private TransactionViewModel viewModel;
    private final LedgerAdapter adapter = new LedgerAdapter();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentTransactionBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);
        binding.rvLedger.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvLedger.setAdapter(adapter);

        binding.btnLedgerSearch.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Tìm kiếm", Toast.LENGTH_SHORT).show());
        binding.btnLedgerFilter.setOnClickListener(v ->
                Toast.makeText(requireContext(), "Lọc", Toast.LENGTH_SHORT).show());

        binding.chipThisMonth.setOnClickListener(v -> selectPeriod(TransactionViewModel.PERIOD_THIS_MONTH));
        binding.chipLastMonth.setOnClickListener(v -> selectPeriod(TransactionViewModel.PERIOD_LAST_MONTH));
        binding.chipCustom.setOnClickListener(v -> {
            selectPeriod(TransactionViewModel.PERIOD_CUSTOM);
            Toast.makeText(requireContext(), com.financeapp.mobile.R.string.ledger_tab_custom,
                    Toast.LENGTH_SHORT).show();
        });

        viewModel.getUi().observe(getViewLifecycleOwner(), model -> {
            if (binding == null || model == null) {
                return;
            }
            binding.textLedgerExpense.setText(model.expenseTotal);
            binding.textLedgerIncome.setText(model.incomeTotal);
            binding.textLedgerBalance.setText(model.balanceText);
            adapter.submit(model.rows);
        });
    }

    private void selectPeriod(int period) {
        binding.chipThisMonth.setChecked(period == TransactionViewModel.PERIOD_THIS_MONTH);
        binding.chipLastMonth.setChecked(period == TransactionViewModel.PERIOD_LAST_MONTH);
        binding.chipCustom.setChecked(period == TransactionViewModel.PERIOD_CUSTOM);
        viewModel.setPeriod(period);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
