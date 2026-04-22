package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

@Dao
public interface TransactionDao {

    @Query("SELECT * FROM transactions " +
           "INNER JOIN wallets ON transactions.wallet_id = wallets.wallet_id " +
           "WHERE wallets.user_id = :uid AND transactions.is_deleted = 0 " +
           "AND transactions.trans_date >= :from AND transactions.trans_date < :to " +
           "ORDER BY transactions.trans_date DESC")
    List<TransactionEntity> getBetweenForUser(String uid, long from, long to);

    @Query("SELECT COALESCE(SUM(amount), 0) FROM transactions " +
           "INNER JOIN wallets ON transactions.wallet_id = wallets.wallet_id " +
           "WHERE wallets.user_id = :uid AND transactions.is_deleted = 0 " +
           "AND transactions.category_id = :categoryId " +
           "AND transactions.trans_date >= :from AND transactions.trans_date < :to")
    double sumExpenseForCategoryBetweenForUser(String uid, long categoryId, long from, long to);

    @Query("SELECT * FROM transactions " +
           "INNER JOIN wallets ON transactions.wallet_id = wallets.wallet_id " +
           "WHERE wallets.user_id = :uid AND transactions.is_deleted = 0 " +
           "ORDER BY transactions.trans_date DESC LIMIT :limit")
    List<TransactionEntity> getRecentForUser(String uid, int limit);

    @Insert
    long insert(TransactionEntity transaction);

    @Update
    void update(TransactionEntity transaction);

    @Query("DELETE FROM transactions WHERE transaction_id = :id")
    void deleteById(long id);
}
