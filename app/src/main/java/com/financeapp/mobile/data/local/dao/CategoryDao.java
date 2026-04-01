package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.financeapp.mobile.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories WHERE kind = :kind ORDER BY name ASC")
    List<CategoryEntity> getByKind(String kind);

    @Insert
    long insert(CategoryEntity category);
}
