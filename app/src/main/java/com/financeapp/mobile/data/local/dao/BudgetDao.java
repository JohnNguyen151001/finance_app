package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.BudgetEntity;

import java.util.List;

@Dao
public interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE user_id = :uid ORDER BY budget_id ASC")
    List<BudgetEntity> getAllForUser(String uid);

    @Query("SELECT * FROM budgets WHERE budget_id = :id LIMIT 1")
    BudgetEntity getById(long id);

    @Query("SELECT * FROM budgets WHERE user_id = :uid AND category_id = :categoryId LIMIT 1")
    BudgetEntity getByCategoryForUser(String uid, long categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Query("DELETE FROM budgets WHERE budget_id = :id")
    void deleteById(long id);
}
