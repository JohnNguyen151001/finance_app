package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "wallets",
        indices = {
                @Index("name"),
                @Index("user_id")
        },
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "uuid",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        })
public class WalletEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "wallet_id")
    public long id;

    @ColumnInfo(name = "user_id")
    public String userId;

    public String name;
    public double balance;

    public String type; // Thêm trường type theo ERD (Vd: Tiền mặt, Thẻ ACB)

    @ColumnInfo(defaultValue = "'VND'")
    public String currency = "VND";

    @ColumnInfo(name = "icon_url")
    public String iconUrl;

    public long createdAt; // Thêm trường createdAt theo ERD

    /** 0 = active, 1 = soft-deleted */
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    public int isDeleted = 0;
}
