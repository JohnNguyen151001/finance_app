package com.financeapp.mobile.ui.report;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.domain.BudgetMonthUtils;
import com.financeapp.mobile.domain.model.TransactionType;
import com.financeapp.mobile.ui.format.MoneyUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportViewModel extends AndroidViewModel {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final MutableLiveData<ReportUiModel> ui = new MutableLiveData<>();
    private int monthOffset = 0;

    // Palette for distinct colors
    private static final int[] CATEGORY_COLORS = {
            Color.parseColor("#2ecc71"), Color.parseColor("#3498db"), Color.parseColor("#9b59b6"),
            Color.parseColor("#f1c40f"), Color.parseColor("#e67e22"), Color.parseColor("#e74c3c"),
            Color.parseColor("#1abc9c"), Color.parseColor("#2c3e50"), Color.parseColor("#d35400"),
            Color.parseColor("#c0392b"), Color.parseColor("#16a085"), Color.parseColor("#27ae60")
    };

    public ReportViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        categoryRepository = new CategoryRepository(application);
        refresh();
    }

    public LiveData<ReportUiModel> getUi() {
        return ui;
    }

    public void setMonthOffset(int offset) {
        this.monthOffset = offset;
        refresh();
    }

    public void refresh() {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
            String uid = u != null ? u.getUid() : "";

            long from = BudgetMonthUtils.monthStartMillisForOffset(monthOffset);
            long to = BudgetMonthUtils.monthEndExclusiveMillisForOffset(monthOffset);

            List<TransactionEntity> list = transactionRepository.getBetween(uid, from, to);
            List<CategoryEntity> allCats = categoryRepository.getAll(uid);
            Map<Long, CategoryEntity> catMap = new HashMap<>();
            for (CategoryEntity c : allCats) catMap.put(c.id, c);

            double income = 0;
            double expense = 0;
            double debt = 0;
            double loan = 0;
            double other = 0;

            Map<Long, Double> incSharesMap = new HashMap<>();
            Map<Long, Double> expSharesMap = new HashMap<>();

            for (TransactionEntity t : list) {
                TransactionType type = safeType(t.type);
                
                if (type == TransactionType.INCOME) {
                    income += t.amount;
                    incSharesMap.put(t.categoryId, incSharesMap.getOrDefault(t.categoryId, 0.0) + t.amount);
                } else if (type == TransactionType.EXPENSE) {
                    expense += t.amount;
                    expSharesMap.put(t.categoryId, expSharesMap.getOrDefault(t.categoryId, 0.0) + t.amount);
                } else if (type == TransactionType.BORROW) {
                    debt += t.amount;
                } else if (type == TransactionType.LEND) {
                    loan += t.amount;
                } else {
                    other += t.amount;
                }
                
                CategoryEntity cat = catMap.get(t.categoryId);
                if (cat != null && type == TransactionType.EXPENSE) {
                    String nameLower = cat.name.toLowerCase();
                    if (nameLower.contains("nợ")) {
                        debt += t.amount;
                        expense -= t.amount;
                        expSharesMap.remove(t.categoryId);
                    } else if (nameLower.contains("cho vay")) {
                        loan += t.amount;
                        expense -= t.amount;
                        expSharesMap.remove(t.categoryId);
                    }
                }
            }

            List<ReportUiModel.CategoryShare> incShares = new ArrayList<>();
            int incColorIdx = 0;
            for (Map.Entry<Long, Double> entry : incSharesMap.entrySet()) {
                CategoryEntity c = catMap.get(entry.getKey());
                String name = (c != null) ? c.name : "Khác";
                float pct = (income > 0) ? (float) (entry.getValue() / income * 100) : 0;
                incShares.add(new ReportUiModel.CategoryShare(name, entry.getValue(), pct, CATEGORY_COLORS[incColorIdx % CATEGORY_COLORS.length]));
                incColorIdx++;
            }

            List<ReportUiModel.CategoryShare> expShares = new ArrayList<>();
            int expColorIdx = 0;
            for (Map.Entry<Long, Double> entry : expSharesMap.entrySet()) {
                CategoryEntity c = catMap.get(entry.getKey());
                String name = (c != null) ? c.name : "Khác";
                float pct = (expense > 0) ? (float) (entry.getValue() / expense * 100) : 0;
                expShares.add(new ReportUiModel.CategoryShare(name, entry.getValue(), pct, CATEGORY_COLORS[expColorIdx % CATEGORY_COLORS.length]));
                expColorIdx++;
            }

            double net = income - expense;

            ui.postValue(new ReportUiModel(
                    MoneyUtils.formatVnd(0), 
                    MoneyUtils.formatVnd(net),
                    MoneyUtils.formatIncome(income),
                    MoneyUtils.formatSignedExpense(expense),
                    MoneyUtils.formatSignedBalance(net),
                    net, income, expense,
                    MoneyUtils.formatVnd(debt),
                    MoneyUtils.formatVnd(loan),
                    MoneyUtils.formatVnd(other),
                    incShares, expShares
            ));
        });
    }

    private static TransactionType safeType(String raw) {
        if (raw == null) return TransactionType.EXPENSE;
        try {
            return TransactionType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return TransactionType.EXPENSE;
        }
    }
}
