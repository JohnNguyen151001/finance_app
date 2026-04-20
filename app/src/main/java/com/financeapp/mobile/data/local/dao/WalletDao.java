package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.WalletEntity;

import java.util.List;

@Dao
public interface WalletDao {

    @Query("SELECT * FROM wallets WHERE userId = :uid AND isDeleted = 0 ORDER BY createdAt DESC")
    List<WalletEntity> getAllForUser(String uid);

    @Query("SELECT * FROM wallets WHERE id = :id LIMIT 1")
    WalletEntity getById(long id);

    @Insert
    long insert(WalletEntity wallet);

    @Update
    void update(WalletEntity wallet);

    @Query("UPDATE wallets SET isDeleted = 1 WHERE id = :id")
    void softDeleteById(long id);
}
