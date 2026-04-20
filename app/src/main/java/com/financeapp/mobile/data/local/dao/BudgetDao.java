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

    @Query("SELECT * FROM budgets WHERE userId = :uid AND monthKey = :monthKey ORDER BY id ASC")
    List<BudgetEntity> getForMonthForUser(String uid, String monthKey);

    @Query("SELECT * FROM budgets WHERE id = :id LIMIT 1")
    BudgetEntity getById(long id);

    @Query("SELECT * FROM budgets WHERE userId = :uid AND monthKey = :monthKey AND categoryId = :categoryId LIMIT 1")
    BudgetEntity getByMonthAndCategoryForUser(String uid, String monthKey, long categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);

    @Query("DELETE FROM budgets WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM budgets WHERE userId = :uid AND monthKey = :monthKey")
    void deleteForMonthForUser(String uid, String monthKey);
}
