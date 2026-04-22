package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.UserEntity;

@Dao
public interface UserDao {
    // Sử dụng IGNORE để không thay thế bản ghi cũ nếu đã tồn tại primary key
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getByEmail(String email);

    @Query("SELECT * FROM users WHERE uuid = :uid LIMIT 1")
    UserEntity getByUid(String uid);

    @Query("SELECT COUNT(*) FROM users")
    int countAll();
}
