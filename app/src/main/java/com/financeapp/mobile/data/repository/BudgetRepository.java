package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.BudgetDao;
import com.financeapp.mobile.data.local.entity.BudgetEntity;

import java.util.List;

public class BudgetRepository {

    private final BudgetDao budgetDao;

    public BudgetRepository(Application application) {
        budgetDao = AppDatabase.getInstance(application).budgetDao();
    }

    public List<BudgetEntity> getForMonth(String uid, String monthKey) {
        return budgetDao.getForMonthForUser(uid, monthKey);
    }

    public BudgetEntity getById(long id) {
        return budgetDao.getById(id);
    }

    public BudgetEntity getByMonthAndCategory(String uid, String monthKey, long categoryId) {
        return budgetDao.getByMonthAndCategoryForUser(uid, monthKey, categoryId);
    }

    public long insert(BudgetEntity entity) {
        return budgetDao.insert(entity);
    }

    public void update(BudgetEntity entity) {
        budgetDao.update(entity);
    }

    public void deleteById(long id) {
        budgetDao.deleteById(id);
    }

    public void deleteForMonth(String uid, String monthKey) {
        budgetDao.deleteForMonthForUser(uid, monthKey);
    }
}
