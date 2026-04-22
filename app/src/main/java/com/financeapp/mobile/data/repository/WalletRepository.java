package com.financeapp.mobile.data.repository;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.WalletDao;
import com.financeapp.mobile.data.local.entity.WalletEntity;

import java.util.List;

/**
 * Quản lý ví (Room).
 */
public class WalletRepository {

    private final WalletDao walletDao;

    public WalletRepository(Application application) {
        walletDao = AppDatabase.getInstance(application).walletDao();
    }

    public List<WalletEntity> getWallets(String uid) {
        return walletDao.getAllForUser(uid);
    }

    public WalletEntity getById(long id) {
        return walletDao.getById(id);
    }

    public long insert(WalletEntity entity) {
        return walletDao.insert(entity);
    }

    public void update(WalletEntity entity) {
        walletDao.update(entity);
    }
}
