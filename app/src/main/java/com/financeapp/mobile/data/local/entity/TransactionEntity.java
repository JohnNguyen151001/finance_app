package com.financeapp.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "transactions",
        indices = {
                @Index("walletId"),
                @Index("categoryId"),
                @Index("occurredAt")
        })
public class TransactionEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long walletId;
    public long categoryId;
    public double amount;
    /** Giá trị tên enum {@link com.financeapp.mobile.domain.model.TransactionType} */
    public String type;
    public String note;
    public long occurredAt;
}
