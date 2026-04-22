package com.financeapp.mobile.ui.home;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.FragmentHomeBinding;
import com.financeapp.mobile.ui.chart.ChartUiHelper;
import com.financeapp.mobile.ui.home.model.HomeUiModel;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel viewModel;
    private final TopSpendingAdapter topAdapter = new TopSpendingAdapter();
    private final RecentAdapter recentAdapter = new RecentAdapter();
    private boolean chartStyled;
    private boolean swipeRefreshPending;
    private ChipGroup.OnCheckedStateChangeListener periodChipListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(
                this,
                new SavedStateViewModelFactory(requireActivity().getApplication(), this))
                .get(HomeViewModel.class);

        binding.rvTopSpending.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvTopSpending.setAdapter(topAdapter);
        binding.rvRecent.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvRecent.setAdapter(recentAdapter);

        binding.textViewReport.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_report, null, topLevelNavOptions(v)));
        binding.textSeeWallets.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_wallet, null, topLevelNavOptions(v)));
        binding.cardWallets.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_wallet, null, topLevelNavOptions(v)));

        binding.btnSearch.setOnClickListener(v ->
                Snackbar.make(v, R.string.home_search_placeholder, Snackbar.LENGTH_SHORT).show());
        binding.btnNotifInner.setOnClickListener(v ->
                Snackbar.make(v, R.string.home_notifications_placeholder, Snackbar.LENGTH_SHORT).show());
        binding.textSeeAllTransactions.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_ledger, null, topLevelNavOptions(v)));

        binding.cardHomeChallenge.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_budget, null, topLevelNavOptions(v)));

        periodChipListener = (group, checkedIds) -> {
            if (checkedIds == null || checkedIds.isEmpty()) {
                return;
            }
            int id = checkedIds.get(0);
            if (id == R.id.chip_week) {
                viewModel.setTopSpendingWeekly(true);
            } else if (id == R.id.chip_month) {
                viewModel.setTopSpendingWeekly(false);
            }
        };
        binding.chipGroupPeriod.setOnCheckedStateChangeListener(periodChipListener);

        binding.swipeHome.setOnRefreshListener(() -> {
            swipeRefreshPending = true;
            viewModel.refresh();
        });

        binding.cardHomeBudget.setContentDescription(getString(R.string.home_budget_card_cd));
        View.OnClickListener budgetNav = v -> navigateFromBudgetCard();
        binding.cardHomeBudget.setOnClickListener(budgetNav);
        binding.btnHomeBudgetCta.setOnClickListener(budgetNav);

        viewModel.getUi().observe(getViewLifecycleOwner(), this::render);
    }

    /** Cùng NavOptions với BottomNavigationView — chuyển tab top-level không làm hỏng back stack. */
    private static NavOptions topLevelNavOptions(@NonNull View anchor) {
        NavController navController = Navigation.findNavController(anchor);
        int startId = NavGraph.findStartDestination(navController.getGraph()).getId();
        return new NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(startId, false, true)
                .build();
    }

    private void navigateFromBudgetCard() {
        HomeUiModel m = viewModel.getUi().getValue();
        if (m == null || m.budgetLoading || m.budgetOverview == null || binding == null) {
            return;
        }
        NavController navController = Navigation.findNavController(binding.getRoot());
        NavOptions options = topLevelNavOptions(binding.getRoot());
        if (m.budgetOverview.empty) {
            navController.navigate(R.id.nav_budget_select_group, null, options);
        } else {
            navController.navigate(R.id.nav_budget, null, options);
        }
    }

    private void bindPeriodChipsFromModel(boolean weekly) {
        if (binding == null) {
            return;
        }
        binding.chipGroupPeriod.setOnCheckedStateChangeListener(null);
        if (weekly) {
            binding.chipWeek.setChecked(true);
        } else {
            binding.chipMonth.setChecked(true);
        }
        binding.chipGroupPeriod.setOnCheckedStateChangeListener(periodChipListener);
    }

    private void render(HomeUiModel model) {
        if (binding == null || model == null) {
            return;
        }
        if (!chartStyled) {
            ChartUiHelper.styleSpendLineChart(binding.chartMonthTrend);
            chartStyled = true;
        }
        binding.textBalance.setText(model.balanceText);
        binding.textWalletName.setText(model.walletName);
        binding.textWalletBalance.setText(model.walletBalanceText);
        binding.textMonthExpense.setText(model.monthExpenseText);
        binding.textMonthIncome.setText(model.monthIncomeText);
        topAdapter.submit(model.topSpending);
        recentAdapter.submit(model.recent);
        ChartUiHelper.bindLineChart(binding.chartMonthTrend, model.monthSpendTrend);

        bindPeriodChipsFromModel(model.topSpendingWeekly);
        bindChallengeCard(model);

        if (model.budgetLoading) {
            binding.progressHomeBudgetLoading.setVisibility(View.VISIBLE);
            binding.layoutHomeBudgetBody.setVisibility(View.INVISIBLE);
            return;
        }
        if (swipeRefreshPending) {
            binding.swipeHome.setRefreshing(false);
            swipeRefreshPending = false;
            binding.textHomeSyncHint.setText(R.string.home_sync_updated);
        }
        binding.progressHomeBudgetLoading.setVisibility(View.GONE);
        binding.layoutHomeBudgetBody.setVisibility(View.VISIBLE);

        HomeUiModel.HomeBudgetOverview b = model.budgetOverview;
        if (b == null) {
            return;
        }
        binding.textHomeBudgetPrimary.setText(b.primaryLine);
        binding.textHomeBudgetSecondary.setText(b.secondaryLine);

        int green = ContextCompat.getColor(requireContext(), R.color.spend_green);
        int red = ContextCompat.getColor(requireContext(), R.color.expense_red);
        int yellow = 0xFFFFC107;
        int toneColor = b.healthTone == 2 ? red : (b.healthTone == 1 ? yellow : green);

        if (b.empty) {
            binding.progressHomeBudgetBar.setVisibility(View.GONE);
            binding.btnHomeBudgetCta.setText(R.string.home_budget_cta_create);
            binding.textHomeBudgetPrimary.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_primary));
            binding.textHomeBudgetSecondary.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_secondary));
        } else {
            binding.progressHomeBudgetBar.setVisibility(View.VISIBLE);
            binding.progressHomeBudgetBar.setProgress(b.progressPct);
            binding.progressHomeBudgetBar.setProgressTintList(ColorStateList.valueOf(toneColor));
            binding.textHomeBudgetPrimary.setTextColor(toneColor);
            binding.textHomeBudgetSecondary.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.text_secondary));
            binding.btnHomeBudgetCta.setText(R.string.home_budget_cta_details);
        }
        binding.btnHomeBudgetCta.setTextColor(green);
    }

    private void bindChallengeCard(HomeUiModel model) {
        if (binding == null) {
            return;
        }
        HomeUiModel.ChallengeSnippet c = model.topChallenge;
        if (c != null) {
            binding.textHomeChallengeEmoji.setText(c.emoji);
            binding.textHomeChallengeTitle.setText(c.title);
            binding.textHomeChallengeSubtitle.setText(c.subtitle);
            binding.progressHomeChallenge.setProgress(Math.min(100, Math.max(0, c.progressPct)));
        } else {
            binding.textHomeChallengeEmoji.setText("\uD83C\uDFAF");
            binding.textHomeChallengeTitle.setText(R.string.challenge_home_title);
            binding.textHomeChallengeSubtitle.setText(R.string.challenge_home_cta);
            binding.progressHomeChallenge.setProgress(0);
        }
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
        chartStyled = false;
    }
}
