package com.financeapp.mobile.ui.category;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.databinding.ItemCategoryRowBinding;

import java.util.ArrayList;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.VH> {

    private static final int[] ICON_BG = {
            0xFFE8F8F0, 0xFFFCE4EC, 0xFFE3F2FD, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA, 0xFFFBE9E7
    };

    private final List<CategoryEntity> data = new ArrayList<>();

    public void submit(List<CategoryEntity> list) {
        data.clear();
        if (list != null) data.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new VH(ItemCategoryRowBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
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
        private final ItemCategoryRowBinding b;

        VH(ItemCategoryRowBinding b) {
            super(b.getRoot());
            this.b = b;
        }

        void bind(CategoryEntity c) {
            b.catIcon.setText(c.iconKey != null ? c.iconKey : "📁");
            GradientDrawable circle = new GradientDrawable();
            circle.setShape(GradientDrawable.OVAL);
            circle.setColor(ICON_BG[(int) (Math.abs(c.id) % ICON_BG.length)]);
            b.catIconBg.setBackground(circle);
            b.catName.setText(c.name);
            b.catWallet.setText("Tiền mặt");
        }
    }
}
