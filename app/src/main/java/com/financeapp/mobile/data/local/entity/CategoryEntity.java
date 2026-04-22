package com.financeapp.mobile.data.local.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "categories",
        indices = {@Index(value = {"user_id", "name", "type"}, unique = true)},
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "uuid",
                        childColumns = "user_id",
                        onDelete = ForeignKey.CASCADE
                )
        })
public class CategoryEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "category_id")
    public long id;

    @ColumnInfo(name = "user_id")
    public String userId;

    public String name;

    /** Loại: INCOME (Thu) hoặc EXPENSE (Chi) */
    public String type;

    @ColumnInfo(name = "color_code")
    public String colorCode;

    @ColumnInfo(name = "icon_name")
    public String iconName;

    /** 0 = active, 1 = soft-deleted */
    @ColumnInfo(name = "is_deleted", defaultValue = "0")
    public int isDeleted = 0;
}
