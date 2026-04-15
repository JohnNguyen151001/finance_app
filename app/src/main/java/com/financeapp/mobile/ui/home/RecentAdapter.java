package com.financeapp.mobile.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.databinding.ItemRecentTransactionBinding;
import com.financeapp.mobile.ui.home.model.HomeUiModel;

import java.util.ArrayList;
import java.util.List;

public class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.VH> {

    private final List<HomeUiModel.RecentRow> data = new ArrayList<>();

    public void submit(List<HomeUiModel.RecentRow> rows) {
        data.clear();
        if (rows != null) {
            data.addAll(rows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemRecentTransactionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(data.get(position));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemRecentTransactionBinding binding;

        VH(ItemRecentTransactionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HomeUiModel.RecentRow row) {
            binding.trIcon.setText(row.icon);
            binding.trTitle.setText(row.title);
            binding.trSubtitle.setText(row.subtitle);
            binding.trAmount.setText(row.amount);
            binding.trAmount.setTextColor(row.amountColorArgb);
        }
    }
}
