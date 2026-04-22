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

    public List<BudgetEntity> getAllForUser(String uid) {
        return budgetDao.getAllForUser(uid);
    }

    public BudgetEntity getById(long id) {
        return budgetDao.getById(id);
    }

    public BudgetEntity getByCategory(String uid, long categoryId) {
        return budgetDao.getByCategoryForUser(uid, categoryId);
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
}
