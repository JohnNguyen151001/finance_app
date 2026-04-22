package com.financeapp.mobile.ui.transaction;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.databinding.ItemLedgerHeaderBinding;
import com.financeapp.mobile.databinding.ItemLedgerLineBinding;
import com.financeapp.mobile.ui.transaction.model.LedgerListItem;

import java.util.ArrayList;
import java.util.List;

public class LedgerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<LedgerListItem> data = new ArrayList<>();

    public void submit(List<LedgerListItem> rows) {
        data.clear();
        if (rows != null) {
            data.addAll(rows);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position).viewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == LedgerListItem.TYPE_HEADER) {
            return new HeaderVH(ItemLedgerHeaderBinding.inflate(inflater, parent, false));
        }
        return new LineVH(ItemLedgerLineBinding.inflate(inflater, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        LedgerListItem item = data.get(position);
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind(item.headerText);
        } else if (holder instanceof LineVH) {
            ((LineVH) holder).bind(item);
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class HeaderVH extends RecyclerView.ViewHolder {
        private final ItemLedgerHeaderBinding binding;

        HeaderVH(ItemLedgerHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(String text) {
            binding.ledgerHeaderText.setText(text);
        }
    }

    static class LineVH extends RecyclerView.ViewHolder {
        private final ItemLedgerLineBinding binding;

        LineVH(ItemLedgerLineBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(LedgerListItem item) {
            binding.ledgerIcon.setText(item.icon);
            binding.ledgerTitle.setText(item.title);
            binding.ledgerSubtitle.setText(item.subtitle);
            binding.ledgerAmount.setText(item.amount);
            binding.ledgerAmount.setTextColor(item.amountColorArgb);
        }
    }
}
