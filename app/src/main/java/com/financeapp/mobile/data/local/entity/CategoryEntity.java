package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "categories",
        indices = {@Index(value = {"userId", "name", "kind"}, unique = true)})
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    /** null = system default category shared by all users */
    public String userId;

    public String name;
    public String iconKey;
    /** INCOME hoặc EXPENSE */
    public String kind;
    public String colorCode;

    /** 0 = active, 1 = soft-deleted */
    @ColumnInfo(defaultValue = "0")
    public int isDeleted = 0;
}
