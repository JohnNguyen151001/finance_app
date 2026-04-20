package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.financeapp.mobile.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories WHERE (userId IS NULL OR userId = :uid) AND isDeleted = 0 AND kind = :kind ORDER BY name ASC")
    List<CategoryEntity> getByKindForUser(String uid, String kind);

    @Query("SELECT * FROM categories WHERE (userId IS NULL OR userId = :uid) AND isDeleted = 0 ORDER BY name ASC")
    List<CategoryEntity> getAllForUser(String uid);

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    CategoryEntity getById(long id);

    @Insert
    long insert(CategoryEntity category);

    /** Dùng cho seeder: kiểm tra đã có category hệ thống chưa */
    @Query("SELECT COUNT(*) FROM categories")
    int countAll();
}
