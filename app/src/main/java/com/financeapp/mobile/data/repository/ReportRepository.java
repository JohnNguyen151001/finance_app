package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.TransactionDao;
import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

/**
 * Báo cáo (tổng hợp từ giao dịch).
 */
public class ReportRepository {

    private final TransactionDao transactionDao;

    public ReportRepository(Application application) {
        transactionDao = AppDatabase.getInstance(application).transactionDao();
    }

    public List<TransactionEntity> getTransactionsInRange(String uid, long fromMillis, long toMillis) {
        return transactionDao.getBetweenForUser(uid, fromMillis, toMillis);
    }
}
