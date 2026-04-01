package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.BudgetDao;
import com.financeapp.mobile.data.local.entity.BudgetEntity;

import java.util.List;

/**
 * Nhóm 4 — Ngân sách.
 */
public class BudgetRepository {

    private final BudgetDao budgetDao;

    public BudgetRepository(Application application) {
        budgetDao = AppDatabase.getInstance(application).budgetDao();
    }

    public List<BudgetEntity> getForMonth(String monthKey) {
        return budgetDao.getForMonth(monthKey);
    }

    public long insert(BudgetEntity entity) {
        return budgetDao.insert(entity);
    }
}
