package com.financeapp.mobile.ui.add;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.data.repository.WalletRepository;
import com.financeapp.mobile.domain.model.TransactionType;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class AddTransactionViewModel extends AndroidViewModel {

    public static final int TAB_EXPENSE = 0;
    public static final int TAB_INCOME = 1;
    public static final int TAB_DEBT = 2;

    private final WalletRepository walletRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    private final MutableLiveData<FormMeta> meta = new MutableLiveData<>();
    private final MutableLiveData<Boolean> saveDone = new MutableLiveData<>();

    private int tab = TAB_EXPENSE;

    public AddTransactionViewModel(@NonNull Application application) {
        super(application);
        walletRepository = new WalletRepository(application);
        categoryRepository = new CategoryRepository(application);
        transactionRepository = new TransactionRepository(application);
        reloadMeta();
    }

    public LiveData<FormMeta> getMeta() { return meta; }
    public LiveData<Boolean> getSaveDone() { return saveDone; }

    public void setTab(int tab) {
        this.tab = tab;
        reloadMeta();
    }

    public void clearSaveDone() { saveDone.setValue(null); }

    private static String uidOrEmpty() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u != null ? u.getUid() : "";
    }

    public void reloadMeta() {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            String uid = uidOrEmpty();
            List<WalletEntity> wallets = walletRepository.getWallets(uid);
            String kind = (tab == TAB_INCOME) ? "INCOME" : "EXPENSE";
            List<CategoryEntity> categories = categoryRepository.getByKind(uid, kind);
            
            meta.postValue(new FormMeta(wallets, categories));
        });
    }

    public void save(long walletId, long categoryId, double amount, String note, long occurredAt) {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            TransactionType type = typeForTab();
            
            TransactionEntity t = new TransactionEntity();
            t.walletId = walletId;
            t.categoryId = categoryId;
            t.amount = amount;
            t.type = type.name();
            t.note = (note != null) ? note : "";
            t.transDate = occurredAt;
            t.isDeleted = 0;
            
            transactionRepository.insert(t);

            WalletEntity w = walletRepository.getById(walletId);
            if (w != null) {
                if (type == TransactionType.INCOME) {
                    w.balance += amount;
                } else {
                    w.balance -= amount;
                }
                walletRepository.update(w);
            }
            
            saveDone.postValue(true);
        });
    }

    private TransactionType typeForTab() {
        if (tab == TAB_INCOME) return TransactionType.INCOME;
        if (tab == TAB_DEBT) return TransactionType.BORROW;
        return TransactionType.EXPENSE;
    }

    public static class FormMeta {
        public final List<WalletEntity> wallets;
        public final List<CategoryEntity> categories;
        public FormMeta(List<WalletEntity> wallets, List<CategoryEntity> categories) {
            this.wallets = (wallets != null) ? wallets : new ArrayList<>();
            this.categories = (categories != null) ? categories : new ArrayList<>();
        }
    }
}
