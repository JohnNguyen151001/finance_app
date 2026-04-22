package com.financeapp.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "budgets",
        indices = {
                @Index(value = {"userId", "monthKey", "categoryId"}, unique = true),
                @Index("userId")
        })
public class BudgetEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String userId;

    /** Định dạng yyyy-MM */
    public String monthKey;
    public long categoryId;
    public double limitAmount;
    /** Tổng đã chi (cập nhật khi có giao dịch) — có thể chuyển sang tính động sau */
    public double spentAmount;
}
