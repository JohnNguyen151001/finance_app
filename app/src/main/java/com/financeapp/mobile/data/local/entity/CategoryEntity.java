package com.financeapp.mobile.data.local.entity;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories", indices = {@Index(value = {"name", "kind"}, unique = true)})
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String name;
    public String iconKey;
    /** INCOME hoặc EXPENSE */
    public String kind;
}
