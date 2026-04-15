package com.financeapp.mobile.ui.budget;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.databinding.ItemCategoryToggleRowBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BudgetGroupSelectAdapter extends RecyclerView.Adapter<BudgetGroupSelectAdapter.VH> {

    public interface OnSelectionChangedListener {
        void onSelectionChanged();
    }

    private static final int[] ICON_BG = {
            0xFFE8F8F0, 0xFFFCE4EC, 0xFFE3F2FD, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA, 0xFFFBE9E7
    };

    private final List<CategoryEntity> items = new ArrayList<>();
    private final Map<Long, Boolean> checked = new HashMap<>();
    private OnSelectionChangedListener selectionListener;

    public void submit(List<CategoryEntity> list) {
        items.clear();
        items.addAll(list);
        for (CategoryEntity c : items) {
            if (!checked.containsKey(c.id)) {
                checked.put(c.id, false);
            }
        }
        boolean any = false;
        for (CategoryEntity c : items) {
            if (Boolean.TRUE.equals(checked.get(c.id))) {
                any = true;
                break;
            }
        }
        if (!items.isEmpty() && !any) {
            checked.put(items.get(0).id, true);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    public void setOnSelectionChangedListener(OnSelectionChangedListener listener) {
        this.selectionListener = listener;
    }

    private void notifySelectionChanged() {
        if (selectionListener != null) {
            selectionListener.onSelectionChanged();
        }
    }

    /** Số nhóm đang bật (không tính fallback mặc định khi submit). */
    public int getSelectedCount() {
        int n = 0;
        for (CategoryEntity c : items) {
            if (Boolean.TRUE.equals(checked.get(c.id))) n++;
        }
        return n;
    }

    public List<Long> getSelectedIds() {
        List<Long> out = new ArrayList<>();
        for (CategoryEntity c : items) {
            if (Boolean.TRUE.equals(checked.get(c.id))) out.add(c.id);
        }
        if (out.isEmpty() && !items.isEmpty()) {
            out.add(items.get(0).id);
        }
        return out;
    }

    /** Bật = tất cả bật; tắt = chỉ giữ nhóm đầu tiên (tránh không chọn gì). */
    public void setAllChecked(boolean on) {
        for (CategoryEntity c : items) {
            checked.put(c.id, on);
        }
        if (!on && !items.isEmpty()) {
            for (CategoryEntity c : items) {
                checked.put(c.id, false);
            }
            checked.put(items.get(0).id, true);
        }
        notifyDataSetChanged();
        notifySelectionChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCategoryToggleRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(this, items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        private final ItemCategoryToggleRowBinding binding;

        VH(ItemCategoryToggleRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(BudgetGroupSelectAdapter adapter, CategoryEntity c) {
            String icon = c.iconKey != null ? c.iconKey : "📁";
            binding.categoryIcon.setText(icon);
            int bg = ICON_BG[(int) (Math.abs(c.id) % ICON_BG.length)];
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(bg);
            binding.categoryIconBg.setBackground(circle);
            binding.categoryName.setText(c.name);
            Boolean on = adapter.checked.get(c.id);
            binding.categorySwitch.setOnCheckedChangeListener(null);
            binding.categorySwitch.setChecked(Boolean.TRUE.equals(on));
            binding.categorySwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                adapter.checked.put(c.id, isChecked);
                adapter.notifySelectionChanged();
            });
        }
    }
}
