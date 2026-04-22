package com.financeapp.mobile.data.bootstrap;

import android.content.Context;
import android.util.Log;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.local.entity.UserEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;

import java.util.Calendar;
import java.util.List;

/**
 * Seed dữ liệu mẫu cho ứng dụng.
 */
public final class DatabaseSeeder {
    private static final String TAG = "DatabaseSeeder";

    private DatabaseSeeder() {
    }

    public static void seedIfEmpty(Context appContext) {
        AppDatabase db = AppDatabase.getInstance(appContext);
        
        if (db.categoryDao().countAll() == 0) {
            Log.d(TAG, "Seeding system categories...");
            insertSystemCategory(db, null, "Ăn uống", "🍔", "EXPENSE", "#FF5733");
            insertSystemCategory(db, null, "Mua sắm", "🛍️", "EXPENSE", "#9C27B0");
            insertSystemCategory(db, null, "Di chuyển", "🚗", "EXPENSE", "#2196F3");
            insertSystemCategory(db, null, "Sức khỏe", "💊", "EXPENSE", "#4CAF50");
            insertSystemCategory(db, null, "Tiền nhà", "🏠", "EXPENSE", "#FFC107");
            insertSystemCategory(db, null, "Giải trí", "🎬", "EXPENSE", "#E91E63");
            insertSystemCategory(db, null, "Giáo dục", "📚", "EXPENSE", "#00BCD4");
            insertSystemCategory(db, null, "Lương", "💵", "INCOME", "#43A047");
            insertSystemCategory(db, null, "Thu nhập khác", "💰", "INCOME", "#8BC34A");
        }
    }

    public static void seedDataForUser(AppDatabase db, String userId) {
        // Kiểm tra xem user đã có ví chưa, nếu chưa mới tạo
        List<WalletEntity> existing = db.walletDao().getAllForUser(userId);
        if (!existing.isEmpty()) {
            Log.d(TAG, "User already has wallets, skipping seed.");
            return;
        }

        // Tạo ví mặc định
        long walletId = insertWallet(db, userId, "Tiền mặt", 1000000, "CASH");

        // Lấy danh sách category để tham chiếu
        List<CategoryEntity> allCats = db.categoryDao().getAllForUser(userId);
        
        CategoryEntity foodCat = null;
        CategoryEntity shoppingCat = null;
        
        for (CategoryEntity c : allCats) {
            if ("Ăn uống".equals(c.name)) foodCat = c;
            if ("Mua sắm".equals(c.name)) shoppingCat = c;
        }

        // Dùng thời gian HIỆN TẠI để hiện lên báo cáo "Tháng này"
        long now = System.currentTimeMillis();

        double totalExpense = 0;
        if (foodCat != null) {
            insertTransaction(db, walletId, foodCat.id, 25000, "EXPENSE", now, "Ăn sáng");
            insertTransaction(db, walletId, foodCat.id, 23000, "EXPENSE", now, "Mua sữa hạt");
            totalExpense += (25000 + 23000);
        }
        
        if (shoppingCat != null) {
            insertTransaction(db, walletId, shoppingCat.id, 500000, "EXPENSE", now, "Mua thảm yoga, gạch yoga");
            totalExpense += 500000;
        }
        
        // Cập nhật lại số dư ví sau khi trừ các giao dịch mẫu
        WalletEntity wallet = db.walletDao().getById(walletId);
        if (wallet != null) {
            wallet.balance = 1000000 - totalExpense;
            db.walletDao().update(wallet);
        }
        Log.d(TAG, "Finished seeding real-time data for user: " + userId);
    }

    private static long insertSystemCategory(AppDatabase db, String userId, String name,
                                             String icon, String type, String color) {
        CategoryEntity c = new CategoryEntity();
        c.userId = userId;
        c.name = name;
        c.iconName = icon;
        c.type = type;
        c.colorCode = color;
        c.isDeleted = 0;
        return db.categoryDao().insert(c);
    }

    private static long insertWallet(AppDatabase db, String userId, String name, double balance, String type) {
        WalletEntity w = new WalletEntity();
        w.userId = userId;
        w.name = name;
        w.balance = balance;
        w.type = type;
        w.currency = "VND";
        w.createdAt = System.currentTimeMillis();
        w.iconUrl = "👛";
        return db.walletDao().insert(w);
    }

    private static void insertTransaction(AppDatabase db, long walletId, long categoryId, double amount, String type, long date, String note) {
        TransactionEntity t = new TransactionEntity();
        t.walletId = walletId;
        t.categoryId = categoryId;
        t.amount = amount;
        t.type = type;
        t.transDate = date;
        t.note = note;
        t.isDeleted = 0;
        db.transactionDao().insert(t);
    }
}
