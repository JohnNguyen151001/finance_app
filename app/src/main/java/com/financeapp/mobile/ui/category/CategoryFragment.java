package com.financeapp.mobile.ui.category;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.databinding.FragmentCategoryBinding;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class CategoryFragment extends Fragment {

    private FragmentCategoryBinding binding;
    private CategoryRepository categoryRepository;
    private final CategoryAdapter adapter = new CategoryAdapter();
    private int currentTab = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCategoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        categoryRepository = new CategoryRepository(requireActivity().getApplication());

        binding.rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCategories.setAdapter(adapter);

        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
        binding.btnAddCategory.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.category_add_new, Toast.LENGTH_SHORT).show());
        binding.btnShowInactive.setOnClickListener(v ->
                Toast.makeText(requireContext(), R.string.category_show_inactive, Toast.LENGTH_SHORT).show());

        binding.tabCategoryType.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                loadForTab(currentTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        loadForTab(0);
    }

    private void loadForTab(int tab) {
        String kind = tab == 0 ? "EXPENSE" : (tab == 1 ? "INCOME" : "DEBT");
        ((FinanceApp) requireActivity().getApplication()).databaseIo().execute(() -> {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            String uid = u != null ? u.getUid() : "";
            List<CategoryEntity> list = categoryRepository.getByKind(uid, kind);
            requireActivity().runOnUiThread(() -> {
                if (binding != null) adapter.submit(list);
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
