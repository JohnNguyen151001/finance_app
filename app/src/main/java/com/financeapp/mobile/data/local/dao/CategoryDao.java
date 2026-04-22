package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.financeapp.mobile.data.local.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("SELECT * FROM categories WHERE (user_id IS NULL OR user_id = :uid) AND is_deleted = 0 AND type = :kind ORDER BY name ASC")
    List<CategoryEntity> getByKindForUser(String uid, String kind);

    @Query("SELECT * FROM categories WHERE (user_id IS NULL OR user_id = :uid) AND is_deleted = 0 ORDER BY name ASC")
    List<CategoryEntity> getAllForUser(String uid);

    @Query("SELECT * FROM categories WHERE category_id = :id LIMIT 1")
    CategoryEntity getById(long id);

    @Insert
    long insert(CategoryEntity category);

    /** Dùng cho seeder: kiểm tra đã có category hệ thống chưa */
    @Query("SELECT COUNT(*) FROM categories")
    int countAll();
}
