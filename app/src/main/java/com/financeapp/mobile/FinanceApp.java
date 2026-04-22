package com.financeapp.mobile;

import android.app.Application;

import com.financeapp.mobile.data.bootstrap.DatabaseSeeder;
import com.financeapp.mobile.data.local.AppDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Application entry — hàng đợi I/O đơn luồng đảm bảo seed chạy trước các truy vấn Room.
 */
public class FinanceApp extends Application {

    private Executor databaseIo;

    @Override
    public void onCreate() {
        super.onCreate();
        databaseIo = Executors.newSingleThreadExecutor();
        databaseIo.execute(() -> DatabaseSeeder.seedIfEmpty(getApplicationContext()));
        AppDatabase.getInstance(this);
    }

    public Executor databaseIo() {
        return databaseIo;
    }
}
