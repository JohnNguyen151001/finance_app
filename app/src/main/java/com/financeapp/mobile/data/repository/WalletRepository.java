package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.WalletDao;
import com.financeapp.mobile.data.local.entity.WalletEntity;

import java.util.List;

/**
 * Nhóm 1 — Quản lý ví (tách UI khỏi Room).
 */
public class WalletRepository {

    private final WalletDao walletDao;

    public WalletRepository(Application application) {
        walletDao = AppDatabase.getInstance(application).walletDao();
    }

    public List<WalletEntity> getWallets() {
        return walletDao.getAll();
    }

    public long insert(WalletEntity entity) {
        return walletDao.insert(entity);
    }
}
