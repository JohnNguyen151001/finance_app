package com.financeapp.mobile.data.local.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.financeapp.mobile.data.local.entity.BudgetEntity;

import java.util.List;

@Dao
public interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE monthKey = :monthKey")
    List<BudgetEntity> getForMonth(String monthKey);

    @Insert
    long insert(BudgetEntity budget);

    @Update
    void update(BudgetEntity budget);
}
