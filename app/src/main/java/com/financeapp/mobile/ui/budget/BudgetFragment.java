package com.financeapp.mobile.ui.budget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.FragmentBudgetBinding;
import com.financeapp.mobile.databinding.ItemChallengeCardBinding;
import com.financeapp.mobile.domain.ChallengeEngine;
import com.financeapp.mobile.ui.budget.model.BudgetRow;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class BudgetFragment extends Fragment {

    private FragmentBudgetBinding binding;
    private BudgetViewModel viewModel;
    private BudgetAdapter adapter;
    private ChallengeCardAdapter challengeAdapter;
    private boolean swipeRefreshPending;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentBudgetBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(BudgetViewModel.class);

        setupRecyclerView();
        setupChallengeRecycler();
        setupToolbar();
        setupButtons();
        observeViewModel();

        binding.swipeBudget.setOnRefreshListener(() -> {
            swipeRefreshPending = true;
            viewModel.refresh();
        });
    }

    private void setupToolbar() {
        binding.toolbarBudget.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_copy_month) {
                confirmCopyToNextMonth();
                return true;
            }
            if (id == R.id.action_help) {
                showHelp();
                return true;
            }
            if (id == R.id.action_delete_all_month) {
                confirmDeleteAllMonth();
                return true;
            }
            return false;
        });
    }

    // ─── Setup ───────────────────────────────────────────────────────────────

    private void setupRecyclerView() {
        adapter = new BudgetAdapter();
        adapter.setListener(new BudgetAdapter.Listener() {
            @Override
            public void onEdit(BudgetRow row) {
                navigateToEdit(row.budgetId, row.categoryId);
            }

            @Override
            public void onDelete(BudgetRow row) {
                confirmDelete(row);
            }

            @Override
            public void onRowClick(BudgetRow row) {
                navigateToEdit(row.budgetId, row.categoryId);
            }
        });

        binding.rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvBudgets.setAdapter(adapter);
        binding.rvBudgets.setHasFixedSize(false);
        binding.rvBudgets.setNestedScrollingEnabled(false);
        attachSwipeToDelete();
    }

    private void setupButtons() {
        // FAB chính
        binding.fabCreateBudget.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_budget_select_group));

        // Nút tạo trong empty state
        binding.btnEmptyCreate.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.nav_budget_select_group));

        binding.btnPrevMonth.setOnClickListener(v -> viewModel.prevMonth());
        binding.btnNextMonth.setOnClickListener(v -> viewModel.nextMonth());

        binding.btnCreateChallenge.setOnClickListener(v -> showCreateChallengeDialog());
    }

    private void setupChallengeRecycler() {
        challengeAdapter = new ChallengeCardAdapter(challengeId -> viewModel.deleteChallenge(challengeId));
        binding.rvChallenges.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvChallenges.setAdapter(challengeAdapter);
        binding.rvChallenges.setNestedScrollingEnabled(false);
    }

    // ─── Observe ─────────────────────────────────────────────────────────────

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading -> {
            if (binding == null) return;
            binding.progressLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
            if (!loading && swipeRefreshPending) {
                binding.swipeBudget.setRefreshing(false);
                swipeRefreshPending = false;
            }
        });

        viewModel.getMonthLabel().observe(getViewLifecycleOwner(), label -> {
            if (binding == null) return;
            binding.textBudgetMonth.setText(label);
            boolean canNext = viewModel.canGoNext();
            binding.btnNextMonth.setEnabled(canNext);
            binding.btnNextMonth.setContentDescription(getString(canNext
                    ? R.string.budget_month_next_cd
                    : R.string.budget_month_next_cd_disabled));
            boolean showChallenges = viewModel.getMonthOffset() == 0;
            binding.layoutChallengeSection.setVisibility(showChallenges ? View.VISIBLE : View.GONE);
        });

        viewModel.getSummary().observe(getViewLifecycleOwner(), s -> {
            if (binding == null || s == null) return;
            binding.textCanSpend.setText(s.canSpendFormatted);
            binding.textStatTotalBudget.setText(s.totalBudgetShort);
            binding.textStatSpent.setText(s.totalSpentFormatted);
            binding.textStatDays.setText(s.daysLeftLabel);
            binding.textGaugeCaption.setText(getString(R.string.budget_gauge_caption,
                    s.totalSpentFormatted, s.totalBudgetShort));
            binding.gaugeBudget.setProgress(s.gaugeSpendRatio);
            int color = s.gaugeSpendRatio >= 1f
                    ? ContextCompat.getColor(requireContext(), R.color.expense_red)
                    : s.gaugeSpendRatio >= 0.8f ? 0xFFFFC107
                    : ContextCompat.getColor(requireContext(), R.color.spend_green);
            binding.textCanSpend.setTextColor(color);
        });

        viewModel.getRows().observe(getViewLifecycleOwner(), rows -> {
            if (binding == null) return;
            adapter.submit(rows);
            int count = rows != null ? rows.size() : 0;
            binding.textBudgetCount.setText(getString(R.string.budget_count_label, count));
            binding.scrollBudget.post(() -> binding.scrollBudget.requestLayout());
        });

        viewModel.getIsEmpty().observe(getViewLifecycleOwner(), empty -> {
            if (binding == null) return;
            binding.layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvBudgets.setVisibility(empty ? View.GONE : View.VISIBLE);
            // Ẩn/hiện FAB dựa trên trạng thái (vẫn hiện để user tạo thêm)
            binding.fabCreateBudget.setVisibility(View.VISIBLE);
        });

        viewModel.getSnackbarEvent().observe(getViewLifecycleOwner(), event -> {
            if (event == null || binding == null) return;
            String msg = event.getContentIfNotHandled();
            if (msg != null) {
                Snackbar.make(binding.getRoot(), msg, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getChallenges().observe(getViewLifecycleOwner(), list -> {
            if (binding == null) return;
            if (viewModel.getMonthOffset() != 0) {
                return;
            }
            List<ChallengeEngine.Progress> data = list != null ? list : new ArrayList<>();
            challengeAdapter.submit(data);
            boolean empty = data.isEmpty();
            binding.textChallengeEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.rvChallenges.setVisibility(empty ? View.GONE : View.VISIBLE);
            binding.scrollBudget.post(() -> binding.scrollBudget.requestLayout());
        });
    }

    private void showCreateChallengeDialog() {
        if (viewModel.getMonthOffset() != 0) {
            return;
        }
        Context ctx = requireContext();
        int pad = (int) (20 * getResources().getDisplayMetrics().density);
        LinearLayout root = new LinearLayout(ctx);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad / 2, pad, pad);

        RadioGroup rg = new RadioGroup(ctx);
        int idNoSpend = View.generateViewId();
        int idBeat = View.generateViewId();
        int idWeekly = View.generateViewId();
        int idPerfect = View.generateViewId();

        RadioButton r1 = new RadioButton(ctx);
        r1.setId(idNoSpend);
        r1.setText(R.string.challenge_type_no_spend);
        RadioButton r2 = new RadioButton(ctx);
        r2.setId(idBeat);
        r2.setText(R.string.challenge_type_beat_last);
        RadioButton r3 = new RadioButton(ctx);
        r3.setId(idWeekly);
        r3.setText(R.string.challenge_type_weekly);
        RadioButton r4 = new RadioButton(ctx);
        r4.setId(idPerfect);
        r4.setText(R.string.challenge_type_budget_perfect);
        rg.addView(r1);
        rg.addView(r2);
        rg.addView(r3);
        rg.addView(r4);
        rg.check(idNoSpend);

        EditText amountInput = new EditText(ctx);
        amountInput.setHint(R.string.challenge_weekly_amount_hint);
        amountInput.setInputType(InputType.TYPE_CLASS_NUMBER);
        amountInput.setVisibility(View.GONE);

        rg.setOnCheckedChangeListener((group, checkedId) ->
                amountInput.setVisibility(checkedId == idWeekly ? View.VISIBLE : View.GONE));

        root.addView(rg);
        root.addView(amountInput);

        new MaterialAlertDialogBuilder(ctx)
                .setTitle(R.string.challenge_type_prompt)
                .setView(root)
                .setPositiveButton(R.string.challenge_dialog_positive, (d, w) -> {
                    int checked = rg.getCheckedRadioButtonId();
                    String typeName;
                    if (checked == idNoSpend) {
                        typeName = ChallengeEngine.Type.NO_SPEND_DAY.name();
                    } else if (checked == idBeat) {
                        typeName = ChallengeEngine.Type.BEAT_LAST_MONTH.name();
                    } else if (checked == idWeekly) {
                        typeName = ChallengeEngine.Type.WEEKLY_LIMIT.name();
                    } else if (checked == idPerfect) {
                        typeName = ChallengeEngine.Type.BUDGET_PERFECT.name();
                    } else {
                        typeName = ChallengeEngine.Type.NO_SPEND_DAY.name();
                    }
                    String weeklyText = checked == idWeekly ? amountInput.getText().toString() : null;
                    viewModel.createChallenge(typeName, weeklyText);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    // ─── Actions ─────────────────────────────────────────────────────────────

    private void navigateToEdit(long budgetId, long categoryId) {
        Bundle args = new Bundle();
        args.putLong(BudgetEditFragment.ARG_BUDGET_ID, budgetId);
        args.putLong(BudgetEditFragment.ARG_CATEGORY_ID, categoryId);
        Navigation.findNavController(requireView()).navigate(R.id.nav_budget_edit, args);
    }

    private void confirmDelete(BudgetRow row) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.budget_delete_title)
                .setMessage(getString(R.string.budget_delete_confirm, row.title))
                .setPositiveButton(R.string.budget_delete_confirm_btn, (d, w) -> viewModel.deleteBudget(row.budgetId))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void confirmCopyToNextMonth() {
        if (viewModel.getMonthOffset() >= 0) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.budget_copy_next_month)
                    .setMessage(R.string.budget_copy_confirm_msg)
                    .setPositiveButton(R.string.budget_copy_confirm_btn, (d, w) ->
                            viewModel.copyBudgetsToNextMonth(null))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            Snackbar.make(binding.getRoot(), R.string.budget_copy_from_past, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteAllMonth() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.budget_delete_all_month)
                .setMessage(R.string.budget_delete_all_confirm)
                .setPositiveButton(R.string.budget_delete_confirm_btn, (d, w) ->
                        viewModel.deleteAllBudgetsForCurrentMonth())
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showHelp() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.budget_help)
                .setMessage(R.string.budget_help_message)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    // ─── Swipe to delete ─────────────────────────────────────────────────────

    private void attachSwipeToDelete() {
        int deleteColor = ContextCompat.getColor(requireContext(), R.color.expense_red);
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh,
                                  @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                int pos = vh.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                BudgetRow row = adapter.getItem(pos);
                confirmDelete(row);
                adapter.notifyItemChanged(pos);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                    @NonNull RecyclerView.ViewHolder vh, float dX, float dY,
                                    int actionState, boolean active) {
                View itemView = vh.itemView;
                Paint paint = new Paint();
                paint.setColor(deleteColor);
                RectF bg = new RectF(itemView.getRight() + dX, itemView.getTop() + 8f,
                        itemView.getRight(), itemView.getBottom() - 8f);
                c.drawRoundRect(bg, 16f, 16f, paint);

                paint.setColor(Color.WHITE);
                paint.setTextSize(48f);
                paint.setTextAlign(Paint.Align.CENTER);
                float iconX = itemView.getRight() - 60f;
                float iconY = (itemView.getTop() + itemView.getBottom()) / 2f + 16f;
                c.drawText("🗑", iconX, iconY, paint);

                super.onChildDraw(c, rv, vh, dX, dY, actionState, active);
            }
        }).attachToRecyclerView(binding.rvBudgets);
    }

    // ─── Lifecycle ───────────────────────────────────────────────────────────

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.refresh();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static final class ChallengeCardAdapter extends RecyclerView.Adapter<ChallengeCardAdapter.VH> {

        interface OnDeleteChallenge {
            void onDelete(long challengeId);
        }

        private final OnDeleteChallenge onDeleteChallenge;
        private final List<ChallengeEngine.Progress> items = new ArrayList<>();

        ChallengeCardAdapter(OnDeleteChallenge onDeleteChallenge) {
            this.onDeleteChallenge = onDeleteChallenge;
        }

        void submit(List<ChallengeEngine.Progress> data) {
            items.clear();
            if (data != null) {
                items.addAll(data);
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemChallengeCardBinding b = ItemChallengeCardBinding.inflate(
                    LayoutInflater.from(parent.getContext()), parent, false);
            return new VH(b);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static final class VH extends RecyclerView.ViewHolder {
            private final ItemChallengeCardBinding b;

            VH(ItemChallengeCardBinding binding) {
                super(binding.getRoot());
                this.b = binding;
            }

            void bind(ChallengeEngine.Progress p, OnDeleteChallenge onDelete) {
                b.textChallengeEmoji.setText(p.emoji);
                b.textChallengeTitle.setText(p.title);
                String sub = p.subtitle;
                if (p.achieved) {
                    sub = sub + "\n" + b.getRoot().getContext().getString(R.string.challenge_achieved);
                }
                b.textChallengeSubtitle.setText(sub);
                b.progressChallenge.setProgress(Math.min(100, Math.max(0, p.progressPct)));
                b.btnChallengeDelete.setOnClickListener(v -> onDelete.onDelete(p.challengeId));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.bind(items.get(position), onDeleteChallenge);
        }
    }
}
