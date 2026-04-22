package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "transactions",
        indices = {
                @Index("walletId"),
                @Index("categoryId"),
                @Index("occurredAt"),
                @Index("userId")
        })
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String userId;

    public long walletId;
    public long categoryId;
    public double amount;
    /** Giá trị tên enum {@link com.financeapp.mobile.domain.model.TransactionType} */
    public String type;
    public String note;
    public long occurredAt;

    /** 0 = active, 1 = soft-deleted */
    @ColumnInfo(defaultValue = "0")
    public int isDeleted = 0;
}
