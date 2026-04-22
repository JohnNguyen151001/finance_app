package com.financeapp.mobile.ui.budget;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.ChallengeDao;
import com.financeapp.mobile.data.local.entity.BudgetEntity;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.ChallengeEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.repository.BudgetRepository;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.domain.BudgetMonthUtils;
import com.financeapp.mobile.domain.ChallengeEngine;
import com.financeapp.mobile.ui.budget.model.BudgetRow;
import com.financeapp.mobile.ui.budget.model.BudgetSummary;
import com.financeapp.mobile.ui.format.MoneyUtils;
import com.financeapp.mobile.ui.util.Event;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * ViewModel dùng chung cho BudgetFragment, BudgetGroupSelectFragment, BudgetEditFragment
 * thông qua requireActivity() — đảm bảo cùng monthOffset và data sau khi lưu.
 */
public class BudgetViewModel extends AndroidViewModel {

    private static final int[] ICON_BG = {
            0xFFE8F8F0, 0xFFFCE4EC, 0xFFE3F2FD, 0xFFFFF3E0, 0xFFF3E5F5, 0xFFE0F7FA, 0xFFFBE9E7
    };

    private static final int MAX_ACTIVE_CHALLENGES = 3;

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final ChallengeDao challengeDao;

    private final MutableLiveData<String> monthLabel = new MutableLiveData<>();
    private final MutableLiveData<BudgetSummary> summary = new MutableLiveData<>();
    private final MutableLiveData<List<BudgetRow>> rows = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isEmpty = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Event<String>> snackbarEvent = new MutableLiveData<>();
    private final MutableLiveData<List<ChallengeEngine.Progress>> challenges =
            new MutableLiveData<>(Collections.emptyList());

    /** Offset tháng so với tháng hiện tại (0 = tháng này, -1 = tháng trước…). */
    private int monthOffset = 0;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private static String uidOrEmpty() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u != null ? u.getUid() : "";
    }

    public BudgetViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        categoryRepository = new CategoryRepository(application);
        transactionRepository = new TransactionRepository(application);
        challengeDao = AppDatabase.getInstance(application).challengeDao();
        refresh();
    }

    public LiveData<String> getMonthLabel() {
        return monthLabel;
    }

    public LiveData<BudgetSummary> getSummary() {
        return summary;
    }

    public LiveData<List<BudgetRow>> getRows() {
        return rows;
    }

    public LiveData<Boolean> getIsEmpty() {
        return isEmpty;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<Event<String>> getSnackbarEvent() {
        return snackbarEvent;
    }

    public LiveData<List<ChallengeEngine.Progress>> getChallenges() {
        return challenges;
    }

    public void createChallenge(@NonNull String typeName, @Nullable String weeklyAmountText) {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            String uid = uidOrEmpty();
            if (challengeDao.countActiveForUser(uid) >= MAX_ACTIVE_CHALLENGES) {
                snackbarEvent.postValue(new Event<>(getApplication().getString(R.string.challenge_max_active)));
                return;
            }
            ChallengeEngine.Type t;
            try {
                t = ChallengeEngine.Type.valueOf(typeName);
            } catch (IllegalArgumentException e) {
                return;
            }
            double weeklyTarget = 0;
            if (t == ChallengeEngine.Type.WEEKLY_LIMIT) {
                if (weeklyAmountText == null || weeklyAmountText.trim().isEmpty()) {
                    snackbarEvent.postValue(new Event<>(
                            getApplication().getString(R.string.challenge_weekly_invalid)));
                    return;
                }
                String digits = weeklyAmountText.trim().replaceAll("[^0-9]", "");
                if (digits.isEmpty()) {
                    snackbarEvent.postValue(new Event<>(
                            getApplication().getString(R.string.challenge_weekly_invalid)));
                    return;
                }
                try {
                    weeklyTarget = Long.parseLong(digits);
                } catch (NumberFormatException ex) {
                    snackbarEvent.postValue(new Event<>(
                            getApplication().getString(R.string.challenge_weekly_invalid)));
                    return;
                }
            }
            if (t == ChallengeEngine.Type.WEEKLY_LIMIT && weeklyTarget <= 0) {
                snackbarEvent.postValue(new Event<>(
                        getApplication().getString(R.string.challenge_weekly_invalid)));
                return;
            }
            ChallengeEntity c = new ChallengeEntity();
            c.userId = uid;
            c.type = t.name();
            c.targetValue = t == ChallengeEngine.Type.WEEKLY_LIMIT ? weeklyTarget : 0;
            c.bestStreak = 0;
            c.active = true;
            c.createdAt = System.currentTimeMillis();
            challengeDao.insert(c);
            snackbarEvent.postValue(new Event<>(getApplication().getString(R.string.challenge_created)));
            refreshSync();
        });
    }

    public void deleteChallenge(long id) {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            challengeDao.deleteById(id);
            snackbarEvent.postValue(new Event<>(getApplication().getString(R.string.challenge_deleted)));
            refreshSync();
        });
    }

    public void prevMonth() {
        monthOffset--;
        refresh();
    }

    public void nextMonth() {
        if (monthOffset < 0) {
            monthOffset++;
            refresh();
        }
    }

    public boolean canGoNext() {
        return monthOffset < 0;
    }

    public int getMonthOffset() {
        return monthOffset;
    }

    public void deleteBudget(long budgetId) {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            budgetRepository.deleteById(budgetId);
            refreshSync();
            snackbarEvent.postValue(new Event<>(
                    getApplication().getString(R.string.budget_snackbar_deleted_one)));
        });
    }

    /** Xoá toàn bộ ngân sách của tháng đang xem (một lần ghi DB + refresh). */
    public void deleteAllBudgetsForCurrentMonth() {
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            String key = BudgetMonthUtils.monthKeyForOffset(monthOffset);
            budgetRepository.deleteForMonth(uidOrEmpty(), key);
            refreshSync();
            snackbarEvent.postValue(new Event<>(
                    getApplication().getString(R.string.budget_snackbar_deleted_all)));
        });
    }

    public void upsertBudget(long categoryId, double limitAmount) {
        upsertBudget(categoryId, limitAmount, null);
    }

    public void upsertBudget(long categoryId, double limitAmount, @Nullable Runnable onComplete) {
        isLoading.postValue(true);
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            try {
                upsertBudgetSync(categoryId, limitAmount);
                refreshSync();
            } finally {
                isLoading.postValue(false);
                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }
            }
        });
    }

    public void upsertBudgetsBatch(long[] categoryIds, double[] amounts, @Nullable Runnable onComplete) {
        if (categoryIds == null || amounts == null || categoryIds.length != amounts.length
                || categoryIds.length == 0) {
            if (onComplete != null) {
                mainHandler.post(onComplete);
            }
            return;
        }
        isLoading.postValue(true);
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            try {
                for (int i = 0; i < categoryIds.length; i++) {
                    upsertBudgetSync(categoryIds[i], amounts[i]);
                }
                refreshSync();
            } finally {
                isLoading.postValue(false);
                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }
            }
        });
    }

    public void copyBudgetsToNextMonth(@Nullable Runnable onComplete) {
        isLoading.postValue(true);
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            try {
                String uid = uidOrEmpty();
                String currentKey = BudgetMonthUtils.monthKeyForOffset(monthOffset);
                String nextKey = BudgetMonthUtils.monthKeyForOffset(monthOffset + 1);
                List<BudgetEntity> current = budgetRepository.getForMonth(uid, currentKey);
                List<BudgetEntity> nextExisting = budgetRepository.getForMonth(uid, nextKey);

                int copied = 0;
                for (BudgetEntity src : current) {
                    boolean alreadyExists = false;
                    for (BudgetEntity ex : nextExisting) {
                        if (ex.categoryId == src.categoryId) {
                            alreadyExists = true;
                            break;
                        }
                    }
                    if (!alreadyExists) {
                        BudgetEntity copy = new BudgetEntity();
                        copy.userId = uid;
                        copy.monthKey = nextKey;
                        copy.categoryId = src.categoryId;
                        copy.limitAmount = src.limitAmount;
                        copy.spentAmount = 0;
                        budgetRepository.insert(copy);
                        copied++;
                    }
                }
                String nextLabel = BudgetMonthUtils.monthLabelForOffset(monthOffset + 1);
                String msg = copied > 0
                        ? getApplication().getString(R.string.budget_copy_toast_ok, copied, nextLabel)
                        : getApplication().getString(R.string.budget_copy_toast_skip, nextLabel);
                snackbarEvent.postValue(new Event<>(msg));
            } finally {
                isLoading.postValue(false);
                if (onComplete != null) {
                    mainHandler.post(onComplete);
                }
            }
        });
    }

    public String getCurrentMonthKey() {
        return BudgetMonthUtils.monthKeyForOffset(monthOffset);
    }

    private void upsertBudgetSync(long categoryId, double limitAmount) {
        String uid = uidOrEmpty();
        String monthKey = BudgetMonthUtils.monthKeyForOffset(monthOffset);
        BudgetEntity existing = budgetRepository.getByMonthAndCategory(uid, monthKey, categoryId);
        if (existing != null) {
            existing.limitAmount = limitAmount;
            budgetRepository.update(existing);
        } else {
            BudgetEntity b = new BudgetEntity();
            b.userId = uid;
            b.monthKey = monthKey;
            b.categoryId = categoryId;
            b.limitAmount = limitAmount;
            b.spentAmount = 0;
            budgetRepository.insert(b);
        }
    }

    public void refresh() {
        isLoading.postValue(true);
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            refreshSync();
            isLoading.postValue(false);
        });
    }

    private void refreshSync() {
        String uid = uidOrEmpty();
        String monthKey = BudgetMonthUtils.monthKeyForOffset(monthOffset);
        long rangeFrom = BudgetMonthUtils.monthStartMillisForOffset(monthOffset);
        long rangeToExclusive = BudgetMonthUtils.monthEndExclusiveMillisForOffset(monthOffset);

        monthLabel.postValue(BudgetMonthUtils.monthLabelForOffset(monthOffset));

        List<BudgetEntity> budgets = budgetRepository.getForMonth(uid, monthKey);
        isEmpty.postValue(budgets.isEmpty());

        double totalLimit = 0;
        double totalSpent = 0;

        for (BudgetEntity b : budgets) {
            double spent = transactionRepository.sumBudgetOutgoingForCategoryBetween(
                    uid, b.categoryId, rangeFrom, rangeToExclusive);
            totalLimit += b.limitAmount;
            totalSpent += spent;
        }

        double canSpend = totalLimit - totalSpent;
        float gaugeRatio = totalLimit > 0 ? (float) (totalSpent / totalLimit) : 0f;

        int daysLeft = BudgetMonthUtils.daysLeftInViewedMonth(monthOffset);
        String daysLabel = daysLeft + " " + getApplication().getString(R.string.budget_days_suffix);

        String canSpendStr = MoneyUtils.formatAmountDotDecimal(Math.max(0, canSpend))
                + getApplication().getString(R.string.currency_suffix);
        String totalShort = totalLimit > 0 ? MoneyUtils.formatShortMillion(totalLimit) : "—";
        String spentStr = totalSpent > 0 ? MoneyUtils.formatShortMillion(totalSpent) : "0";

        summary.postValue(new BudgetSummary(canSpendStr, totalShort, spentStr, daysLabel, gaugeRatio));

        boolean showTodayChip = monthOffset == 0;
        int green = ContextCompat.getColor(getApplication(), R.color.spend_green);
        int red = ContextCompat.getColor(getApplication(), R.color.expense_red);
        int yellow = 0xFFFFC107;

        List<BudgetRow> out = new ArrayList<>();
        for (BudgetEntity b : budgets) {
            double spent = transactionRepository.sumBudgetOutgoingForCategoryBetween(
                    uid, b.categoryId, rangeFrom, rangeToExclusive);
            CategoryEntity cat = categoryRepository.getById(b.categoryId);
            String title = cat != null ? cat.name : "?";
            String icon = cat != null && cat.iconKey != null ? cat.iconKey : "📁";
            int bg = ICON_BG[(int) (Math.abs(b.categoryId) % ICON_BG.length)];
            double remaining = b.limitAmount - spent;
            boolean over = remaining < 0;
            double pctD = b.limitAmount <= 0 ? 0 : spent * 100.0 / b.limitAmount;
            int pct = (int) Math.min(100, Math.round(pctD));
            boolean nearLimit = !over && pctD >= 80;

            int remColor = over ? red : (nearLimit ? yellow : green);
            String limitLine = MoneyUtils.formatAmountDotDecimal(b.limitAmount)
                    + getApplication().getString(R.string.currency_suffix);
            String remainingLine = getApplication().getString(R.string.budget_remaining_prefix)
                    + " " + MoneyUtils.formatAmountDotDecimal(Math.max(0, remaining))
                    + getApplication().getString(R.string.currency_suffix);
            String spentLine = getApplication().getString(R.string.budget_spent_prefix)
                    + " " + MoneyUtils.formatAmountDotDecimal(spent)
                    + getApplication().getString(R.string.currency_suffix);
            out.add(new BudgetRow(icon, bg, title, limitLine, remainingLine,
                    pct, over, remColor, showTodayChip, b.id, b.categoryId, spentLine, nearLimit || over));
        }
        out.sort(Comparator
                .comparing((BudgetRow r) -> r.overBudget).reversed()
                .thenComparing((BudgetRow r) -> r.progress, Comparator.reverseOrder()));
        rows.postValue(out);

        if (monthOffset != 0) {
            challenges.postValue(Collections.emptyList());
            return;
        }
        List<ChallengeEntity> activeChallenges = challengeDao.getActiveForUser(uid);
        List<TransactionEntity> thisMonthTx =
                transactionRepository.getBetween(uid, rangeFrom, rangeToExclusive);
        long lastFrom = BudgetMonthUtils.monthStartMillisForOffset(monthOffset - 1);
        long lastTo = BudgetMonthUtils.monthEndExclusiveMillisForOffset(monthOffset - 1);
        List<TransactionEntity> lastMonthTx = transactionRepository.getBetween(uid, lastFrom, lastTo);
        long now = System.currentTimeMillis();
        long weekFrom = now - 7L * 86_400_000L;
        List<TransactionEntity> weekTx = transactionRepository.getBetween(uid, weekFrom, now + 1);

        for (ChallengeEngine.Progress p : ChallengeEngine.evaluate(
                getApplication(),
                monthOffset,
                uid,
                activeChallenges,
                thisMonthTx,
                lastMonthTx,
                weekTx,
                budgets,
                transactionRepository,
                rangeFrom,
                rangeToExclusive)) {
            if (ChallengeEngine.Type.NO_SPEND_DAY.name().equals(p.type)) {
                for (ChallengeEntity ce : activeChallenges) {
                    if (ce.id == p.challengeId && p.currentStreak > ce.bestStreak) {
                        ce.bestStreak = p.currentStreak;
                        challengeDao.update(ce);
                        break;
                    }
                }
            }
        }
        activeChallenges = challengeDao.getActiveForUser(uid);
        challenges.postValue(ChallengeEngine.evaluate(
                getApplication(),
                monthOffset,
                uid,
                activeChallenges,
                thisMonthTx,
                lastMonthTx,
                weekTx,
                budgets,
                transactionRepository,
                rangeFrom,
                rangeToExclusive));
    }
}
