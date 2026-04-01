package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.TransactionDao;
import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

/**
 * Nhóm 2 — Giao dịch.
 */
public class TransactionRepository {

    private final TransactionDao transactionDao;

    public TransactionRepository(Application application) {
        transactionDao = AppDatabase.getInstance(application).transactionDao();
    }

    public List<TransactionEntity> getBetween(long fromMillis, long toMillis) {
        return transactionDao.getBetween(fromMillis, toMillis);
    }

    public long insert(TransactionEntity entity) {
        return transactionDao.insert(entity);
    }
}
