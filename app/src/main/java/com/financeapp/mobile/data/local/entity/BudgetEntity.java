package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "budgets",
        indices = {
                @Index("user_id"),
                @Index("category_id")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "uuid",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CategoryEntity.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.CASCADE
                )
        })
public class BudgetEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "budget_id")
    public long id;

    @ColumnInfo(name = "user_id")
    public String userId;

    @ColumnInfo(name = "category_id")
    public long categoryId;

    @ColumnInfo(name = "limit_amount")
    public double limitAmount;

    @ColumnInfo(name = "start_date")
    public long startDate;

    @ColumnInfo(name = "end_date")
    public long endDate;
}
