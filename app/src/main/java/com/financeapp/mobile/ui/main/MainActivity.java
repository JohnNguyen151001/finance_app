package com.financeapp.mobile.ui.main;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // FragmentContainerView adds NavHostFragment asynchronously; wait one frame.
        binding.getRoot().post(this::setupNavigation);
    }

    private void setupNavigation() {
        if (binding == null) {
            return;
        }
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            return;
        }
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        binding.fabAdd.setOnClickListener(v -> navController.navigate(R.id.nav_add_transaction));

        navController.addOnDestinationChangedListener((ctrl, dest, args) -> {
            if (binding == null) {
                return;
            }
            int id = dest.getId();
            boolean hide = id == R.id.nav_add_transaction
                    || id == R.id.nav_wallet
                    || id == R.id.nav_report
                    || id == R.id.nav_budget_select_group
                    || id == R.id.nav_budget_edit
                    || id == R.id.nav_category;
            int vis = hide ? View.GONE : View.VISIBLE;
            binding.bottomNav.setVisibility(vis);
            binding.fabAdd.setVisibility(vis);
        });
    }
}
