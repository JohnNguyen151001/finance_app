package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE userId = :uid AND isDeleted = 0 AND occurredAt >= :from AND occurredAt < :to ORDER BY occurredAt DESC")
    List<TransactionEntity> getBetweenForUser(String uid, long from, long to);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :uid AND isDeleted = 0 AND categoryId = :categoryId AND type = :expenseType AND occurredAt >= :from AND occurredAt < :to")
    double sumExpenseForCategoryBetweenForUser(String uid, long categoryId, String expenseType, long from, long to);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE userId = :uid AND isDeleted = 0 AND categoryId = :categoryId AND (type = :expenseType OR type = :borrowType) AND occurredAt >= :from AND occurredAt < :to")
    double sumBudgetOutgoingForCategoryBetweenForUser(String uid, long categoryId, String expenseType, String borrowType, long from, long to);

    @Query("SELECT * FROM transactions WHERE userId = :uid AND isDeleted = 0 ORDER BY occurredAt DESC LIMIT :limit")
    List<TransactionEntity> getRecentForUser(String uid, int limit);

    @Insert
    long insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE id = :id")
    void deleteById(long id);
}
