package com.financeapp.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "budgets",
        indices = {@Index(value = {"monthKey", "categoryId"}, unique = true)})
public class BudgetEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Định dạng yyyy-MM */
    public String monthKey;
    public long categoryId;
    public double limitAmount;
    /** Tổng đã chi (cập nhật khi có giao dịch) — có thể chuyển sang tính động sau */
    public double spentAmount;
}
