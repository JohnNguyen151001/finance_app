package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.TransactionDao;
import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

/**
 * Giao dịch.
 */
public class TransactionRepository {

    private final TransactionDao transactionDao;

    public TransactionRepository(Application application) {
        transactionDao = AppDatabase.getInstance(application).transactionDao();
    }

    public List<TransactionEntity> getBetween(String uid, long fromMillis, long toMillis) {
        return transactionDao.getBetweenForUser(uid, fromMillis, toMillis);
    }

    public List<TransactionEntity> getRecent(String uid, int limit) {
        return transactionDao.getRecentForUser(uid, limit);
    }

    public long insert(TransactionEntity entity) {
        return transactionDao.insert(entity);
    }

    public double sumExpenseForCategoryBetween(String uid, long categoryId, long fromMillis, long toMillisExclusive) {
        return transactionDao.sumExpenseForCategoryBetweenForUser(
                uid,
                categoryId,
                fromMillis,
                toMillisExclusive);
    }

    public void update(TransactionEntity entity) {
        transactionDao.update(entity);
    }

    public void deleteById(long id) {
        transactionDao.deleteById(id);
    }
}
