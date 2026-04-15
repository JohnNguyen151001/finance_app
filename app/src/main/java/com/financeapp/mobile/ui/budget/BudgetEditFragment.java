package com.financeapp.mobile.ui.budget;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.entity.BudgetEntity;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.repository.BudgetRepository;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.domain.BudgetMonthUtils;
import com.financeapp.mobile.databinding.FragmentBudgetEditBinding;
import com.financeapp.mobile.ui.budget.model.BudgetEditLine;
import com.financeapp.mobile.ui.format.MoneyUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Thiết lập / chỉnh sửa hạn mức.
 * <ul>
 *   <li>Từ danh sách (edit): {@link #ARG_CATEGORY_ID}, {@link #ARG_BUDGET_ID}</li>
 *   <li>Từ chọn nhóm (tạo mới): {@link #ARG_CATEGORY_IDS} (1 hoặc nhiều nhóm)</li>
 * </ul>
 */
public class BudgetEditFragment extends Fragment {

    public static final String ARG_CATEGORY_ID = "categoryId";
    public static final String ARG_BUDGET_ID = "budgetId";
    /** Mảng id từ màn chọn nhóm: 1 phần tử = single, >1 = batch. */
    public static final String ARG_CATEGORY_IDS = "categoryIds";

    private FragmentBudgetEditBinding binding;
    private BudgetViewModel budgetViewModel;

    private long categoryId = -1;
    private long budgetId = -1;
    private long[] categoryIdsFromSelect;
    private boolean multiMode;

    /** Spent thực tế trong tháng — dùng để tính preview %. */
    private double currentSpentAmount = 0;

    private BudgetAmountRowsAdapter batchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetEditBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        budgetViewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        parseArgs();
        setupMode();
        setupCommonButtons();
        observeLoading();
        loadCategoryInfo();
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    private void parseArgs() {
        if (getArguments() == null) return;
        categoryIdsFromSelect = getArguments().getLongArray(ARG_CATEGORY_IDS);
        categoryId = getArguments().getLong(ARG_CATEGORY_ID, -1);
        budgetId = getArguments().getLong(ARG_BUDGET_ID, -1);

        if (categoryIdsFromSelect != null && categoryIdsFromSelect.length > 1) {
            multiMode = true;
        } else if (categoryIdsFromSelect != null && categoryIdsFromSelect.length == 1) {
            multiMode = false;
            categoryId = categoryIdsFromSelect[0];
        }
    }

    private void setupMode() {
        if (multiMode) {
            binding.sectionSingle.setVisibility(View.GONE);
            binding.sectionMulti.setVisibility(View.VISIBLE);
            binding.textToolbarTitle.setText(R.string.budget_edit_multi_title);
            binding.btnSaveBudget.setText(
                    getString(R.string.budget_save_batch, categoryIdsFromSelect.length));
            binding.btnDeleteBudget.setVisibility(View.GONE);

            batchAdapter = new BudgetAmountRowsAdapter();
            binding.rvBudgetAmounts.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.rvBudgetAmounts.setAdapter(batchAdapter);
            binding.rvBudgetAmounts.setNestedScrollingEnabled(false);

            binding.btnApplyAll.setOnClickListener(v -> applyAllAmount());

            budgetViewModel.getMonthLabel().observe(getViewLifecycleOwner(), label -> {
                if (binding != null) binding.textMultiMonth.setText(label);
            });
        } else {
            binding.sectionSingle.setVisibility(View.VISIBLE);
            binding.sectionMulti.setVisibility(View.GONE);

            if (budgetId > 0) {
                binding.textToolbarTitle.setText(R.string.budget_edit_from_list);
                binding.btnDeleteBudget.setVisibility(View.VISIBLE);
                binding.btnDeleteBudget.setOnClickListener(v -> confirmDeleteThis());
            } else {
                binding.textToolbarTitle.setText(R.string.budget_add_new_title);
                binding.btnDeleteBudget.setVisibility(View.GONE);
            }
            binding.btnSaveBudget.setText(R.string.budget_save);

            binding.chipAmount500k.setOnClickListener(v -> setAmount(500_000));
            binding.chipAmount1m.setOnClickListener(v -> setAmount(1_000_000));
            binding.chipAmount2m.setOnClickListener(v -> setAmount(2_000_000));
            binding.chipAmount3m.setOnClickListener(v -> setAmount(3_000_000));
            binding.chipAmount5m.setOnClickListener(v -> setAmount(5_000_000));

            binding.inputLimit.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save();
                    return true;
                }
                return false;
            });

            // Real-time preview % khi nhập
            binding.inputLimit.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
                @Override public void onTextChanged(CharSequence s, int i, int b, int c) {}
                @Override
                public void afterTextChanged(Editable s) {
                    updateLimitPreview(s != null ? s.toString() : "");
                }
            });

            budgetViewModel.getMonthLabel().observe(getViewLifecycleOwner(), label -> {
                if (binding != null) binding.editMonthLabel.setText(label);
            });
        }
    }

    private void setupCommonButtons() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnSaveBudget.setOnClickListener(v -> save());
    }

    private void observeLoading() {
        budgetViewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.btnSaveBudget.setEnabled(!loading);
            binding.btnSaveBudget.setAlpha(loading ? 0.6f : 1f);
        });
    }

    // ─── Load data ───────────────────────────────────────────────────────────

    private void loadCategoryInfo() {
        if (multiMode) {
            loadMultiLines();
            return;
        }

        ((FinanceApp) requireActivity().getApplication()).databaseIo().execute(() -> {
            BudgetRepository budgetRepo = new BudgetRepository(requireActivity().getApplication());
            CategoryRepository catRepo = new CategoryRepository(requireActivity().getApplication());
            TransactionRepository txRepo = new TransactionRepository(requireActivity().getApplication());

            BudgetEntity existing = budgetId > 0 ? budgetRepo.getById(budgetId) : null;
            if (categoryId < 0 && existing != null) categoryId = existing.categoryId;

            CategoryEntity cat = categoryId >= 0 ? catRepo.getById(categoryId) : null;

            // Tính spent thực từ giao dịch
            double spent = 0;
            if (categoryId >= 0) {
                int off = budgetViewModel.getMonthOffset();
                long from = BudgetMonthUtils.monthStartMillisForOffset(off);
                long toEx = BudgetMonthUtils.monthEndExclusiveMillisForOffset(off);
                spent = txRepo.sumBudgetOutgoingForCategoryBetween(categoryId, from, toEx);
            }
            final double finalSpent = spent;
            final BudgetEntity finalExisting = existing;
            final CategoryEntity finalCat = cat;

            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;

                if (finalCat != null) {
                    binding.editIcon.setText(finalCat.iconKey != null ? finalCat.iconKey : "📁");
                    binding.editCategoryName.setText(finalCat.name);
                } else {
                    binding.editCategoryName.setText("—");
                }

                currentSpentAmount = finalSpent;

                if (finalExisting != null) {
                    binding.inputLimit.setText(String.valueOf((long) finalExisting.limitAmount));
                    showSpentCard(finalSpent, finalExisting.limitAmount);
                }
            });
        });
    }

    private void loadMultiLines() {
        long[] ids = categoryIdsFromSelect;
        if (ids == null || ids.length == 0) return;

        ((FinanceApp) requireActivity().getApplication()).databaseIo().execute(() -> {
            BudgetRepository budgetRepo = new BudgetRepository(requireActivity().getApplication());
            CategoryRepository catRepo = new CategoryRepository(requireActivity().getApplication());
            String monthKey = budgetViewModel.getCurrentMonthKey();

            List<BudgetEditLine> lines = new ArrayList<>();
            for (long id : ids) {
                CategoryEntity cat = catRepo.getById(id);
                BudgetEntity b = budgetRepo.getByMonthAndCategory(monthKey, id);
                double pref = b != null ? b.limitAmount : 0;
                String name = cat != null ? cat.name : "?";
                String icon = cat != null && cat.iconKey != null ? cat.iconKey : "📁";
                lines.add(new BudgetEditLine(id, name, icon, pref));
            }

            requireActivity().runOnUiThread(() -> {
                if (binding == null || batchAdapter == null) return;
                batchAdapter.submit(lines);
                binding.btnSaveBudget.setText(getString(R.string.budget_save_batch, lines.size()));
            });
        });
    }

    // ─── UI helpers ──────────────────────────────────────────────────────────

    private void setAmount(double amount) {
        binding.inputLimit.setText(String.valueOf((long) amount));
        binding.inputLimit.setSelection(binding.inputLimit.getText() != null
                ? binding.inputLimit.getText().length() : 0);
    }

    private void showSpentCard(double spent, double limit) {
        binding.cardSpentInfo.setVisibility(View.VISIBLE);
        binding.textCurrentSpent.setText(MoneyUtils.formatVnd(spent));
        binding.textCurrentLimit.setText(MoneyUtils.formatVnd(limit));

        int pct = limit > 0 ? (int) Math.min(100, Math.round(spent * 100.0 / limit)) : 0;
        binding.textCurrentPct.setText(pct + "%");
        binding.progressSpent.setProgress(pct);

        int color = pct >= 100
                ? ContextCompat.getColor(requireContext(), R.color.expense_red)
                : pct >= 80 ? 0xFFFFC107
                : ContextCompat.getColor(requireContext(), R.color.spend_green);
        binding.textCurrentPct.setTextColor(color);
        binding.progressSpent.setProgressTintList(ColorStateList.valueOf(color));
    }

    private void updateLimitPreview(String raw) {
        if (currentSpentAmount <= 0) {
            binding.textLimitPreview.setVisibility(View.GONE);
            return;
        }
        String cleaned = raw.trim().replace(",", "");
        if (cleaned.isEmpty()) {
            binding.textLimitPreview.setVisibility(View.GONE);
            return;
        }
        try {
            double limit = Double.parseDouble(cleaned);
            if (limit <= 0) {
                binding.textLimitPreview.setVisibility(View.GONE);
                return;
            }
            int pct = (int) Math.min(100, Math.round(currentSpentAmount * 100.0 / limit));
            String preview = String.format(Locale.getDefault(),
                    "Đã chi %s = %d%% hạn mức mới",
                    MoneyUtils.formatVnd(currentSpentAmount), pct);
            binding.textLimitPreview.setText(preview);
            binding.textLimitPreview.setVisibility(View.VISIBLE);
            int color = pct >= 100
                    ? ContextCompat.getColor(requireContext(), R.color.expense_red)
                    : pct >= 80 ? 0xFFFFC107
                    : ContextCompat.getColor(requireContext(), R.color.text_secondary);
            binding.textLimitPreview.setTextColor(color);
        } catch (NumberFormatException e) {
            binding.textLimitPreview.setVisibility(View.GONE);
        }
    }

    private void applyAllAmount() {
        String raw = binding.inputApplyAll.getText() != null
                ? binding.inputApplyAll.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            binding.layoutApplyAll.setError(getString(R.string.budget_limit_required));
            return;
        }
        binding.layoutApplyAll.setError(null);
        try {
            double amount = Double.parseDouble(raw.replace(",", ""));
            if (amount <= 0) {
                binding.layoutApplyAll.setError(getString(R.string.budget_limit_required));
                return;
            }
            batchAdapter.applySameAmount(amount);
        } catch (NumberFormatException e) {
            binding.layoutApplyAll.setError(getString(R.string.budget_limit_required));
        }
    }

    // ─── Save ────────────────────────────────────────────────────────────────

    private void save() {
        if (multiMode) {
            saveBatch();
        } else {
            saveSingle();
        }
    }

    private void saveSingle() {
        String raw = binding.inputLimit.getText() != null
                ? binding.inputLimit.getText().toString().trim() : "";
        if (raw.isEmpty()) {
            binding.layoutLimit.setError(getString(R.string.budget_limit_required));
            return;
        }
        binding.layoutLimit.setError(null);

        double amount;
        try {
            amount = Double.parseDouble(raw.replace(",", ""));
        } catch (NumberFormatException e) {
            binding.layoutLimit.setError(getString(R.string.budget_limit_required));
            return;
        }
        if (amount <= 0) {
            binding.layoutLimit.setError(getString(R.string.budget_limit_required));
            return;
        }
        if (categoryId < 0) {
            Toast.makeText(requireContext(), R.string.budget_limit_required, Toast.LENGTH_SHORT).show();
            return;
        }
        budgetViewModel.upsertBudget(categoryId, amount, () -> {
            if (!isAdded()) return;
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void saveBatch() {
        if (batchAdapter == null) return;
        double[] amounts = batchAdapter.parseAllAmountsOrNull();
        if (amounts == null) {
            Toast.makeText(requireContext(), R.string.budget_batch_amount_required, Toast.LENGTH_SHORT).show();
            return;
        }
        long[] ids = batchAdapter.getCategoryIds();
        budgetViewModel.upsertBudgetsBatch(ids, amounts, () -> {
            if (!isAdded()) return;
            Navigation.findNavController(requireView()).navigateUp();
        });
    }

    private void confirmDeleteThis() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.budget_delete_title)
                .setMessage(R.string.budget_delete_this_confirm)
                .setPositiveButton(R.string.budget_delete_confirm_btn, (d, w) -> {
                    budgetViewModel.deleteBudget(budgetId);
                    if (isAdded()) Navigation.findNavController(requireView()).navigateUp();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        batchAdapter = null;
    }
}
