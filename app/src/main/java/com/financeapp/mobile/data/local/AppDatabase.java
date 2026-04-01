package com.financeapp.mobile.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.financeapp.mobile.data.local.dao.BudgetDao;
import com.financeapp.mobile.data.local.dao.CategoryDao;
import com.financeapp.mobile.data.local.dao.TransactionDao;
import com.financeapp.mobile.data.local.dao.WalletDao;
import com.financeapp.mobile.data.local.entity.BudgetEntity;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;

@Database(
        entities = {
                WalletEntity.class,
                CategoryEntity.class,
                TransactionEntity.class,
                BudgetEntity.class
        },
        version = 1,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finance_app.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract WalletDao walletDao();

    public abstract CategoryDao categoryDao();

    public abstract TransactionDao transactionDao();

    public abstract BudgetDao budgetDao();
}
