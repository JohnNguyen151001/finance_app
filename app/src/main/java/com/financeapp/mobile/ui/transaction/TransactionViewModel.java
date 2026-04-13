package com.financeapp.mobile.ui.transaction;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.data.repository.WalletRepository;
import com.financeapp.mobile.domain.model.TransactionType;
import com.financeapp.mobile.ui.format.DateDisplayUtils;
import com.financeapp.mobile.ui.format.MoneyUtils;
import com.financeapp.mobile.ui.transaction.model.LedgerListItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TransactionViewModel extends AndroidViewModel {

    public static final int PERIOD_THIS_MONTH = 0;
    public static final int PERIOD_LAST_MONTH = 1;
    public static final int PERIOD_CUSTOM = 2;

    private final MutableLiveData<LedgerUiModel> ui = new MutableLiveData<>();
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final WalletRepository walletRepository;
    private int period = PERIOD_THIS_MONTH;

    public TransactionViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        categoryRepository = new CategoryRepository(application);
        walletRepository = new WalletRepository(application);
        refresh();
    }

    public LiveData<LedgerUiModel> getUi() {
        return ui;
    }

    public void setPeriod(int period) {
        this.period = period;
        refresh();
    }

    public void refresh() {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> ui.postValue(build()));
    }

    private LedgerUiModel build() {
        long[] range = monthRange(period);
        long from = range[0];
        long to = range[1];
        List<TransactionEntity> list = transactionRepository.getBetween(from, to);
        list.sort(Comparator.comparingLong((TransactionEntity t) -> t.occurredAt).reversed());

        double expense = 0;
        double income = 0;
        for (TransactionEntity t : list) {
            TransactionType type = safeType(t.type);
            if (type == TransactionType.EXPENSE || type == TransactionType.BORROW) {
                expense += t.amount;
            } else if (type == TransactionType.INCOME) {
                income += t.amount;
            }
        }
        double balance = income - expense;

        List<LedgerListItem> rows = new ArrayList<>();
        SimpleDateFormat dayKeyFmt = new SimpleDateFormat("yyyyMMdd", Locale.US);
        SimpleDateFormat headerFmt = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        String lastKey = null;
        for (TransactionEntity t : list) {
            String key = dayKeyFmt.format(new Date(t.occurredAt));
            if (!key.equals(lastKey)) {
                rows.add(LedgerListItem.header(headerFmt.format(new Date(t.occurredAt))));
                lastKey = key;
            }
            CategoryEntity cat = categoryRepository.getById(t.categoryId);
            WalletEntity wallet = walletRepository.getById(t.walletId);
            String icon = cat != null && cat.iconKey != null ? cat.iconKey : "💸";
            String catName = cat != null ? cat.name : "";
            String walletName = wallet != null ? wallet.name : "";
            String title = t.note != null && !t.note.isEmpty() ? t.note : catName;
            String subtitle = catName + " · " + walletName;
            TransactionType type = safeType(t.type);
            String amountText;
            int color;
            if (type == TransactionType.INCOME) {
                amountText = "+" + MoneyUtils.formatVnd(t.amount);
                color = ContextCompat.getColor(getApplication(), R.color.spend_green);
            } else {
                amountText = MoneyUtils.formatSignedExpense(t.amount);
                color = ContextCompat.getColor(getApplication(), R.color.expense_red);
            }
            rows.add(LedgerListItem.line(icon, title, subtitle, amountText, color));
        }

        return new LedgerUiModel(
                MoneyUtils.formatVnd(expense),
                MoneyUtils.formatVnd(income),
                MoneyUtils.formatSignedBalance(balance),
                rows
        );
    }

    private long[] monthRange(int p) {
        int effective = p == PERIOD_CUSTOM ? PERIOD_THIS_MONTH : p;
        Calendar start = Calendar.getInstance();
        if (effective == PERIOD_LAST_MONTH) {
            start.add(Calendar.MONTH, -1);
        }
        start.set(Calendar.DAY_OF_MONTH, 1);
        start.set(Calendar.HOUR_OF_DAY, 0);
        start.set(Calendar.MINUTE, 0);
        start.set(Calendar.SECOND, 0);
        start.set(Calendar.MILLISECOND, 0);
        long from = start.getTimeInMillis();
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.MONTH, 1);
        long to = end.getTimeInMillis();
        return new long[]{from, to};
    }

    private static TransactionType safeType(String raw) {
        if (raw == null) {
            return TransactionType.EXPENSE;
        }
        try {
            return TransactionType.valueOf(raw);
        } catch (IllegalArgumentException e) {
            return TransactionType.EXPENSE;
        }
    }
}
