package com.financeapp.mobile.ui.wallet;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.entity.WalletEntity;
import com.financeapp.mobile.databinding.ItemWalletRowBinding;
import com.financeapp.mobile.ui.format.MoneyUtils;

import java.util.ArrayList;
import java.util.List;

public class WalletAdapter extends RecyclerView.Adapter<WalletAdapter.VH> {

    public interface OnWalletClickListener {
        void onWalletClick(WalletEntity wallet);
    }

    private final List<WalletEntity> data = new ArrayList<>();
    private final OnWalletClickListener listener;

    public WalletAdapter(OnWalletClickListener listener) {
        this.listener = listener;
    }

    public void submit(List<WalletEntity> wallets) {
        data.clear();
        if (wallets != null) {
            data.addAll(wallets);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemWalletRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        WalletEntity wallet = data.get(position);
        holder.bind(wallet);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onWalletClick(wallet);
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        private final ItemWalletRowBinding binding;

        VH(ItemWalletRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(WalletEntity w) {
            String icon = w.iconUrl != null ? w.iconUrl : "💼";
            binding.walletIcon.setText(icon);
            binding.walletName.setText(w.name);
            binding.walletType.setText(w.type);
            binding.walletBalance.setText(MoneyUtils.formatSignedBalance(w.balance));
            int color = w.balance < 0
                    ? ContextCompat.getColor(binding.getRoot().getContext(), R.color.expense_red)
                    : ContextCompat.getColor(binding.getRoot().getContext(), R.color.spend_green);
            binding.walletBalance.setTextColor(color);
        }
    }
}
