package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "wallets",
        indices = {
                @Index("name"),
                @Index("userId")
        })
public class WalletEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** Firebase UID; null for legacy rows before migration. */
    public String userId;

    public String name;
    /** Ví dụ: CASH, BANK, CREDIT, GOAL */
    public String type;
    public double balance;
    public String iconKey;

    @ColumnInfo(defaultValue = "'VND'")
    public String currency = "VND";

    public long createdAt;

    /** 0 = active, 1 = soft-deleted */
    @ColumnInfo(defaultValue = "0")
    public int isDeleted = 0;
}
