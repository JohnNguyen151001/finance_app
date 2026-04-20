package com.financeapp.mobile.data.bootstrap;

import android.content.Context;

import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.entity.CategoryEntity;

/**
 * Seed danh mục hệ thống (userId = null) một lần khi DB chưa có category.
 * Không seed ví / giao dịch / ngân sách mock — user tạo sau khi đăng nhập.
 */
public final class DatabaseSeeder {

    private DatabaseSeeder() {
    }

    public static void seedIfEmpty(Context appContext) {
        AppDatabase db = AppDatabase.getInstance(appContext);
        if (db.categoryDao().countAll() > 0) {
            return;
        }
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

    private static void insertSystemCategory(AppDatabase db, String userId, String name,
                                             String icon, String kind, String color) {
        CategoryEntity c = new CategoryEntity();
        c.userId = userId;
        c.name = name;
        c.iconKey = icon;
        c.kind = kind;
        c.colorCode = color;
        c.isDeleted = 0;
        db.categoryDao().insert(c);
    }
}
