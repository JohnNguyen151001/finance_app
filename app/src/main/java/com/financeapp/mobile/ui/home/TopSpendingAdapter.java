package com.financeapp.mobile.ui.home;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.databinding.ItemTopSpendingBinding;
import com.financeapp.mobile.ui.home.model.HomeUiModel;

import java.util.ArrayList;
import java.util.List;

public class TopSpendingAdapter extends RecyclerView.Adapter<TopSpendingAdapter.VH> {

    private final List<HomeUiModel.TopSpendingRow> data = new ArrayList<>();

    public void submit(List<HomeUiModel.TopSpendingRow> rows) {
        data.clear();
        if (rows != null) {
            data.addAll(rows);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemTopSpendingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private final ItemTopSpendingBinding binding;

        VH(ItemTopSpendingBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(HomeUiModel.TopSpendingRow row) {
            binding.topIcon.setText(row.icon);
            binding.topName.setText(row.name);
            binding.topPercent.setText(row.percent + "%");
            binding.topProgress.setProgress(row.progress);
        }
    }
}
