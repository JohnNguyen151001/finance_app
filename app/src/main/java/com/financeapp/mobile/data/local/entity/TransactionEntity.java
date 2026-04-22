package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "transactions",
        indices = {
                @Index("wallet_id"),
                @Index("category_id"),
                @Index("trans_date")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = WalletEntity.class,
                        parentColumns = "wallet_id",
                        childColumns = "wallet_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = CategoryEntity.class,
                        parentColumns = "category_id",
                        childColumns = "category_id",
                        onDelete = ForeignKey.RESTRICT
                )
        })
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "transaction_id")
    public long id;

    @ColumnInfo(name = "wallet_id")
    public long walletId;

    @ColumnInfo(name = "category_id")
    public long categoryId;

    public double amount;

    public String type; // Thêm lại trường type (INCOME/EXPENSE) để dùng trong logic

    @ColumnInfo(name = "trans_date")
    public long transDate;

    public String note;

    /** 0 = active, 1 = soft-deleted */
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    public int isDeleted = 0;
}
