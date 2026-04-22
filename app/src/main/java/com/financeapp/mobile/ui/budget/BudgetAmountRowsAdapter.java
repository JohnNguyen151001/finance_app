package com.financeapp.mobile.ui.budget;

import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.databinding.ItemBudgetAmountRowBinding;
import com.financeapp.mobile.ui.budget.model.BudgetEditLine;

import java.util.ArrayList;
import java.util.List;

public class BudgetAmountRowsAdapter extends RecyclerView.Adapter<BudgetAmountRowsAdapter.VH> {

    private static final int[] ICON_BG = {
            0xFFE8F8F0, 0xFFFCE4EC, 0xFFE3F2FD, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA, 0xFFFBE9E7
    };

    private final List<BudgetEditLine> lines = new ArrayList<>();
    /** Đồng bộ với từng dòng — tránh mất dữ liệu khi ViewHolder recycle. */
    private String[] draftAmounts = new String[0];

    public void submit(List<BudgetEditLine> data) {
        lines.clear();
        if (data != null) lines.addAll(data);
        draftAmounts = new String[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            double p = lines.get(i).prefilledLimit;
            draftAmounts[i] = p > 0 ? String.valueOf((long) p) : "";
        }
        notifyDataSetChanged();
    }

    public void applySameAmount(double amount) {
        if (amount <= 0) return;
        String s = String.valueOf((long) amount);
        for (int i = 0; i < draftAmounts.length; i++) {
            draftAmounts[i] = s;
        }
        notifyItemRangeChanged(0, getItemCount(), "amount");
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty() && "amount".equals(payloads.get(0))) {
            holder.binding.inputRowAmount.setText(draftAmounts[position]);
            return;
        }
        BudgetEditLine line = lines.get(position);
        holder.bind(line, position, draftAmounts);
    }

    /**
     * @return null nếu thiếu hoặc số không hợp lệ
     */
    public double[] parseAllAmountsOrNull() {
        int n = draftAmounts.length;
        double[] out = new double[n];
        for (int i = 0; i < n; i++) {
            String raw = draftAmounts[i] != null ? draftAmounts[i].trim() : "";
            if (raw.isEmpty()) return null;
            try {
                double v = Double.parseDouble(raw.replace(",", ""));
                if (v <= 0) return null;
                out[i] = v;
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return out;
    }

    public long[] getCategoryIds() {
        long[] ids = new long[lines.size()];
        for (int i = 0; i < lines.size(); i++) {
            ids[i] = lines.get(i).categoryId;
        }
        return ids;
    }

    public int getLineCount() {
        return lines.size();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemBudgetAmountRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        BudgetEditLine line = lines.get(position);
        holder.bind(line, position, draftAmounts);
    }

    @Override
    public int getItemCount() {
        return lines.size();
    }

    class VH extends RecyclerView.ViewHolder {
        final ItemBudgetAmountRowBinding binding;
        private TextWatcher textWatcher;

        VH(ItemBudgetAmountRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BudgetEditLine line, int position, String[] drafts) {
            binding.rowIcon.setText(line.iconEmoji);
            int bg = ICON_BG[(int) (Math.abs(line.categoryId) % ICON_BG.length)];
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(bg);
            binding.rowIconBg.setBackground(circle);
            binding.rowName.setText(line.name);

            if (textWatcher != null) {
                binding.inputRowAmount.removeTextChangedListener(textWatcher);
            }
            binding.inputRowAmount.setText(drafts[position]);
            textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) { }

                @Override
                public void afterTextChanged(Editable s) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && pos < drafts.length) {
                        drafts[pos] = s != null ? s.toString() : "";
                    }
                }
            };
            binding.inputRowAmount.addTextChangedListener(textWatcher);
        }
    }
}
