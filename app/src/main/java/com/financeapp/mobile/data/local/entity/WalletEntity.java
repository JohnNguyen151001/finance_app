package com.financeapp.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "wallets", indices = {@Index(value = "name")})
public class WalletEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    /** Ví dụ: CASH, BANK, CREDIT, GOAL */
    public String type;
    public double balance;
    public String iconKey;
    public long createdAt;
}
