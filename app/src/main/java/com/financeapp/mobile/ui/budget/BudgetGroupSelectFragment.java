package com.financeapp.mobile.ui.budget;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.financeapp.mobile.R;
import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.databinding.FragmentBudgetSelectGroupBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BudgetGroupSelectFragment extends Fragment {

    private FragmentBudgetSelectGroupBinding binding;
    private final BudgetGroupSelectAdapter adapter = new BudgetGroupSelectAdapter();
    private final List<CategoryEntity> all = new ArrayList<>();
    private CategoryRepository categoryRepository;

    /** Debounce search 300ms */
    private final Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetSelectGroupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryRepository = new CategoryRepository(requireActivity().getApplication());

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(adapter);
        binding.rvCategories.setHasFixedSize(false);
        binding.rvCategories.setNestedScrollingEnabled(false);

        // Cập nhật counter khi selection thay đổi
        adapter.setOnSelectionChangedListener(() -> {
            if (binding == null) return;
            updateSelectionCount();
        });

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        binding.btnDone.setOnClickListener(v -> {
            List<Long> selected = adapter.getSelectedIds();
            if (selected.isEmpty()) {
                Toast.makeText(requireContext(), R.string.budget_select_min_one, Toast.LENGTH_SHORT).show();
                return;
            }
            long[] arr = new long[selected.size()];
            for (int i = 0; i < selected.size(); i++) arr[i] = selected.get(i);

            Bundle args = new Bundle();
            args.putLongArray(BudgetEditFragment.ARG_CATEGORY_IDS, arr);
            args.putLong(BudgetEditFragment.ARG_CATEGORY_ID, -1L);
            args.putLong(BudgetEditFragment.ARG_BUDGET_ID, -1L);
            Navigation.findNavController(v).navigate(R.id.nav_budget_edit, args);
        });

        // Search với debounce 300ms
        binding.inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
                String query = s != null ? s.toString() : "";
                searchRunnable = () -> filter(query);
                searchHandler.postDelayed(searchRunnable, 300);
            }
        });

        // Toggle tất cả
        binding.switchAllGroups.setOnCheckedChangeListener((buttonView, isChecked) -> {
            adapter.setAllChecked(isChecked);
            updateSelectionCount();
        });

        loadCategories();
    }

    private void loadCategories() {
        ((FinanceApp) requireActivity().getApplication()).databaseIo().execute(() -> {
            List<CategoryEntity> list = categoryRepository.getByKind("EXPENSE");
            requireActivity().runOnUiThread(() -> {
                if (binding == null) return;
                all.clear();
                all.addAll(list);
                String q = binding.inputSearch.getText() != null
                        ? binding.inputSearch.getText().toString() : "";
                filter(q);
                updateSelectionCount();
            });
        });
    }

    private void filter(String q) {
        String query = q.trim().toLowerCase(Locale.ROOT);
        List<CategoryEntity> out = new ArrayList<>();
        for (CategoryEntity c : all) {
            if (query.isEmpty() || c.name.toLowerCase(Locale.ROOT).contains(query)) {
                out.add(c);
            }
        }
        adapter.submit(out);
        if (binding != null) {
            binding.scrollSelectGroup.post(() -> binding.scrollSelectGroup.requestLayout());
        }
    }

    private void updateSelectionCount() {
        if (binding == null) return;
        int count = adapter.getSelectedCount();
        binding.textSelectionCount.setText(
                getString(R.string.budget_selection_count, count));
        // Đổi màu nút Xong khi có ít nhất 1 nhóm
        binding.btnDone.setAlpha(count > 0 ? 1f : 0.4f);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        binding = null;
    }
}
