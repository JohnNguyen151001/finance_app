package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.TransactionDao;
import com.financeapp.mobile.data.local.entity.TransactionEntity;

import java.util.List;

/**
 * Nhóm 3 — Báo cáo (tổng hợp từ giao dịch; mở rộng thêm cache sau).
 */
public class ReportRepository {

    private final TransactionDao transactionDao;

    public ReportRepository(Application application) {
        transactionDao = AppDatabase.getInstance(application).transactionDao();
    }

    public List<TransactionEntity> getTransactionsInRange(long fromMillis, long toMillis) {
        return transactionDao.getBetween(fromMillis, toMillis);
    }
}
