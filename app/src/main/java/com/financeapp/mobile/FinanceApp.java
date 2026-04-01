package com.financeapp.mobile;

import android.app.Application;

import com.financeapp.mobile.data.local.AppDatabase;

/**
 * Application entry — khởi tạo singleton (database, DI container sau này).
 */
public class FinanceApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppDatabase.getInstance(this);
    }
}
