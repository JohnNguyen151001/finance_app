package com.financeapp.mobile.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.financeapp.mobile.data.local.dao.BudgetDao;
import com.financeapp.mobile.data.local.dao.CategoryDao;
import com.financeapp.mobile.data.local.dao.ChallengeDao;
import com.financeapp.mobile.data.local.dao.TransactionDao;
import com.financeapp.mobile.data.local.dao.WalletDao;
import com.financeapp.mobile.data.local.entity.BudgetEntity;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.ChallengeEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;

@Database(
        entities = {
                WalletEntity.class,
                CategoryEntity.class,
                TransactionEntity.class,
                BudgetEntity.class,
                ChallengeEntity.class
        },
        version = 3,
        exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("CREATE TABLE IF NOT EXISTS `challenges` ("
                    + "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + "`type` TEXT NOT NULL, "
                    + "`targetValue` REAL NOT NULL, "
                    + "`bestStreak` INTEGER NOT NULL, "
                    + "`active` INTEGER NOT NULL, "
                    + "`createdAt` INTEGER NOT NULL)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_challenges_active` "
                    + "ON `challenges` (`active`)");
        }
    };

    /**
     * Thêm userId / currency / soft-delete; đổi unique index categories & budgets.
     */
    static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase db) {
            db.execSQL("ALTER TABLE wallets ADD COLUMN userId TEXT");
            db.execSQL("ALTER TABLE wallets ADD COLUMN currency TEXT NOT NULL DEFAULT 'VND'");
            db.execSQL("ALTER TABLE wallets ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0");

            db.execSQL("ALTER TABLE categories ADD COLUMN userId TEXT");
            db.execSQL("ALTER TABLE categories ADD COLUMN colorCode TEXT");
            db.execSQL("ALTER TABLE categories ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0");

            db.execSQL("ALTER TABLE transactions ADD COLUMN userId TEXT");
            db.execSQL("ALTER TABLE transactions ADD COLUMN isDeleted INTEGER NOT NULL DEFAULT 0");

            db.execSQL("ALTER TABLE budgets ADD COLUMN userId TEXT");

            db.execSQL("ALTER TABLE challenges ADD COLUMN userId TEXT");

            db.execSQL("DROP INDEX IF EXISTS `index_categories_name_kind`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_userId_name_kind` "
                    + "ON `categories` (`userId`, `name`, `kind`)");

            db.execSQL("DROP INDEX IF EXISTS `index_budgets_monthKey_categoryId`");
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_budgets_userId_monthKey_categoryId` "
                    + "ON `budgets` (`userId`, `monthKey`, `categoryId`)");

            db.execSQL("CREATE INDEX IF NOT EXISTS `index_wallets_userId` ON `wallets` (`userId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_transactions_userId` ON `transactions` (`userId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_challenges_userId` ON `challenges` (`userId`)");
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_budgets_userId` ON `budgets` (`userId`)");
        }
    };

    public static AppDatabase getInstance(@NonNull Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "finance_app.db")
                            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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

    public abstract ChallengeDao challengeDao();
}
