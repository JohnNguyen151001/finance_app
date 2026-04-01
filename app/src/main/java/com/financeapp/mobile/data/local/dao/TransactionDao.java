package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE occurredAt >= :from AND occurredAt < :to ORDER BY occurredAt DESC")
    List<TransactionEntity> getBetween(long from, long to);

    @Insert
    long insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);
}
