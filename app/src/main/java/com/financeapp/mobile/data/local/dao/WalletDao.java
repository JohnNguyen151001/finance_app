package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.WalletEntity;

import java.util.List;

@Dao
public interface WalletDao {

    @Query("SELECT * FROM wallets WHERE user_id = :uid AND is_deleted = 0")
    List<WalletEntity> getAllForUser(String uid);

    @Query("SELECT * FROM wallets WHERE wallet_id = :id LIMIT 1")
    WalletEntity getById(long id);

    @Insert
    long insert(WalletEntity wallet);

    @Update
    void update(WalletEntity wallet);

    @Query("UPDATE wallets SET is_deleted = 1 WHERE wallet_id = :id")
    void softDeleteById(long id);
}
