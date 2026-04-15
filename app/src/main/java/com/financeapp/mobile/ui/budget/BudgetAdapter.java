package com.financeapp.mobile.ui.budget;

import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.R;
import com.financeapp.mobile.databinding.ItemBudgetRowBinding;
import com.financeapp.mobile.ui.budget.model.BudgetRow;

import java.util.ArrayList;
import java.util.List;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.VH> {

    public interface Listener {
        void onEdit(BudgetRow row);
        void onDelete(BudgetRow row);
        void onRowClick(BudgetRow row);
    }

    private final List<BudgetRow> data = new ArrayList<>();
    private Listener listener;

    public void setListener(Listener l) {
        this.listener = l;
    }

    public void submit(List<BudgetRow> newRows) {
        if (newRows == null) newRows = new ArrayList<>();
        DiffUtil.DiffResult diff = DiffUtil.calculateDiff(new BudgetDiffCallback(data, newRows));
        data.clear();
        data.addAll(newRows);
        diff.dispatchUpdatesTo(this);
    }

    public BudgetRow getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemBudgetRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BudgetRow row = data.get(position);
        holder.bind(row);
        holder.binding.btnEditBudget.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(row);
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onRowClick(row);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    // ─── DiffUtil ────────────────────────────────────────────────────────────

    private static class BudgetDiffCallback extends DiffUtil.Callback {
        private final List<BudgetRow> oldList;
        private final List<BudgetRow> newList;

        BudgetDiffCallback(List<BudgetRow> oldList, List<BudgetRow> newList) {
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() { return oldList.size(); }

        @Override
        public int getNewListSize() { return newList.size(); }

        @Override
        public boolean areItemsTheSame(int oldPos, int newPos) {
            return oldList.get(oldPos).budgetId == newList.get(newPos).budgetId;
        }

        @Override
        public boolean areContentsTheSame(int oldPos, int newPos) {
            BudgetRow o = oldList.get(oldPos);
            BudgetRow n = newList.get(newPos);
            return o.progress == n.progress
                    && o.overBudget == n.overBudget
                    && o.hasWarning == n.hasWarning
                    && o.limitLine.equals(n.limitLine)
                    && o.remainingLine.equals(n.remainingLine)
                    && o.spentLine.equals(n.spentLine);
        }
    }

    // ─── ViewHolder ──────────────────────────────────────────────────────────

    static class VH extends RecyclerView.ViewHolder {
        final ItemBudgetRowBinding binding;

        VH(ItemBudgetRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BudgetRow row) {
            binding.budgetIcon.setText(row.icon);
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(row.iconBgColorArgb);
            binding.budgetIconBg.setBackground(circle);

            binding.budgetTitle.setText(row.title);
            binding.chipToday.setVisibility(row.showTodayTag ? View.VISIBLE : View.GONE);
            binding.budgetLimitLine.setText(row.limitLine);
            binding.budgetRemaining.setText(row.remainingLine);
            binding.budgetRemaining.setTextColor(row.remainingColorArgb);

            int progress = Math.min(100, row.progress);
            binding.budgetProgress.setProgress(progress);
            int barColor = row.overBudget
                    ? ContextCompat.getColor(binding.getRoot().getContext(), R.color.expense_red)
                    : row.remainingColorArgb;
            binding.budgetProgress.setProgressTintList(ColorStateList.valueOf(barColor));

            binding.textPct.setText(binding.getRoot().getContext().getString(
                    R.string.budget_pct_used_line, progress));
            binding.textPct.setTextColor(row.remainingColorArgb);
            binding.textBudgetSpentAmount.setText(row.spentLine);

            // Badge cảnh báo
            binding.badgeWarning.setVisibility(row.hasWarning ? View.VISIBLE : View.GONE);
        }
    }
}
