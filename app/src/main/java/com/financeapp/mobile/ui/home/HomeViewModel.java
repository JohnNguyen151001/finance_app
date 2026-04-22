package com.financeapp.mobile.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.SavedStateHandle;

import com.financeapp.mobile.FinanceApp;
import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.AppDatabase;
import com.financeapp.mobile.data.local.dao.ChallengeDao;
import com.financeapp.mobile.data.local.entity.BudgetEntity;
import com.financeapp.mobile.data.local.entity.CategoryEntity;
import com.financeapp.mobile.data.local.entity.ChallengeEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.local.entity.WalletEntity;
import com.financeapp.mobile.data.repository.BudgetRepository;
import com.financeapp.mobile.data.repository.CategoryRepository;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.data.repository.WalletRepository;
import com.financeapp.mobile.domain.BudgetMonthUtils;
import com.financeapp.mobile.domain.ChallengeEngine;
import com.financeapp.mobile.domain.model.TransactionType;
import com.financeapp.mobile.ui.chart.ChartPoint;
import com.financeapp.mobile.ui.format.DateDisplayUtils;
import com.financeapp.mobile.ui.format.MoneyUtils;
import com.financeapp.mobile.ui.home.model.HomeUiModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeViewModel extends AndroidViewModel {

    private static final String KEY_WEEKLY_TOP_SPENDING = "weekly_top_spending";

    private final SavedStateHandle savedStateHandle;
    private final MutableLiveData<HomeUiModel> ui = new MutableLiveData<>();
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final BudgetRepository budgetRepository;
    private final ChallengeDao challengeDao;
    /** Tab "Chi tiêu nhiều nhất": true = 7 ngày gần nhất trong tháng, false = cả tháng. */
    private boolean weeklyTopSpending;

    public HomeViewModel(@NonNull Application application, @NonNull SavedStateHandle savedStateHandle) {
        super(application);
        this.savedStateHandle = savedStateHandle;
        Boolean savedWeekly = savedStateHandle.get(KEY_WEEKLY_TOP_SPENDING);
        if (savedWeekly != null) {
            weeklyTopSpending = savedWeekly;
        }
        walletRepository = new WalletRepository(application);
        transactionRepository = new TransactionRepository(application);
        categoryRepository = new CategoryRepository(application);
        budgetRepository = new BudgetRepository(application);
        challengeDao = AppDatabase.getInstance(application).challengeDao();
        refresh();
    }

    public LiveData<HomeUiModel> getUi() {
        return ui;
    }

    public void setTopSpendingWeekly(boolean week) {
        if (weeklyTopSpending == week) {
            return;
        }
        weeklyTopSpending = week;
        savedStateHandle.set(KEY_WEEKLY_TOP_SPENDING, week);
        refresh();
    }

    public void refresh() {
        HomeUiModel prev = ui.getValue();
        if (prev != null) {
            ui.setValue(prev.withBudgetLoading(true));
        }
        ((FinanceApp) getApplication()).databaseIo().execute(() -> {
            HomeUiModel model = buildModel();
            ui.postValue(model);
        });
    }

    private static String uidOrEmpty() {
        FirebaseUser u = FirebaseAuth.getInstance().getCurrentUser();
        return u != null ? u.getUid() : "";
    }

    private HomeUiModel buildModel() {
        String uid = uidOrEmpty();
        List<WalletEntity> wallets = walletRepository.getWallets(uid);
        double total = 0;
        for (WalletEntity w : wallets) {
            total += w.balance;
        }
        WalletEntity primary = wallets.isEmpty() ? null : wallets.get(0);

        long from = BudgetMonthUtils.monthStartMillisForOffset(0);
        long to = BudgetMonthUtils.monthEndExclusiveMillisForOffset(0);
        Calendar monthStart = BudgetMonthUtils.calendarForOffset(0);
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);

        List<TransactionEntity> monthTx = transactionRepository.getBetween(uid, from, to);
        double expenseSum = 0;
        double incomeSum = 0;
        for (TransactionEntity t : monthTx) {
            TransactionType type = safeType(t.type);
            if (type == TransactionType.EXPENSE || type == TransactionType.BORROW) {
                expenseSum += t.amount;
            } else if (type == TransactionType.INCOME) {
                incomeSum += t.amount;
            }
        }

        long now = System.currentTimeMillis();
        long topAggFrom = weeklyTopSpending ? Math.max(from, now - 7L * 86_400_000L) : from;
        Map<Long, Double> expenseByCat = new HashMap<>();
        for (TransactionEntity t : monthTx) {
            if (t.occurredAt < topAggFrom) {
                continue;
            }
            TransactionType type = safeType(t.type);
            if (type == TransactionType.EXPENSE || type == TransactionType.BORROW) {
                expenseByCat.merge(t.categoryId, t.amount, Double::sum);
            }
        }

        List<Map.Entry<Long, Double>> sorted = new ArrayList<>(expenseByCat.entrySet());
        sorted.sort(Comparator.comparingDouble((Map.Entry<Long, Double> e) -> e.getValue()).reversed());

        List<HomeUiModel.TopSpendingRow> top = new ArrayList<>();
        double topSum = 0;
        for (Map.Entry<Long, Double> e : expenseByCat.entrySet()) {
            topSum += e.getValue();
        }
        int totalPctBase = topSum > 0 ? (int) Math.round(topSum) : 1;
        int count = 0;
        for (Map.Entry<Long, Double> e : sorted) {
            if (count >= 4) {
                break;
            }
            CategoryEntity cat = categoryRepository.getById(e.getKey());
            String name = cat != null ? cat.name : "?";
            String icon = cat != null && cat.iconKey != null ? cat.iconKey : "📁";
            int pct = (int) Math.round(e.getValue() * 100.0 / totalPctBase);
            int prog = Math.min(100, pct);
            top.add(new HomeUiModel.TopSpendingRow(icon, name, pct, prog));
            count++;
        }

        List<TransactionEntity> recentEntities = transactionRepository.getRecent(uid, 8);
        List<HomeUiModel.RecentRow> recent = new ArrayList<>();
        for (TransactionEntity t : recentEntities) {
            CategoryEntity cat = categoryRepository.getById(t.categoryId);
            String icon = cat != null && cat.iconKey != null ? cat.iconKey : "💸";
            String catName = cat != null ? cat.name : "";
            String day = DateDisplayUtils.formatTransactionSubtitle(t.occurredAt);
            String subtitle = catName + " · " + day;
            TransactionType type = safeType(t.type);
            String amountText;
            int colorArgb;
            if (type == TransactionType.INCOME) {
                amountText = "+" + MoneyUtils.formatVnd(t.amount);
                colorArgb = ContextCompat.getColor(getApplication(), R.color.spend_green);
            } else {
                amountText = MoneyUtils.formatSignedExpense(t.amount);
                colorArgb = ContextCompat.getColor(getApplication(), R.color.expense_red);
            }
            String title = t.note != null && !t.note.isEmpty() ? t.note : catName;
            recent.add(new HomeUiModel.RecentRow(icon, title, subtitle, amountText, colorArgb));
        }

        String walletName = primary != null ? primary.name : "—";
        String walletBal = primary != null
                ? MoneyUtils.formatSignedBalance(primary.balance)
                : MoneyUtils.formatSignedBalance(0);

        List<ChartPoint> trend = buildMonthSpendTrend(monthStart, monthTx);

        HomeUiModel.HomeBudgetOverview budgetOverview = buildBudgetOverview(from, to);
        HomeUiModel.ChallengeSnippet topChallenge = buildTopChallengeSnippet(from, to, monthTx);

        return new HomeUiModel(
                MoneyUtils.formatSignedBalance(total),
                walletName,
                walletBal,
                MoneyUtils.formatVnd(expenseSum),
                MoneyUtils.formatVnd(incomeSum),
                top,
                recent,
                trend,
                weeklyTopSpending,
                budgetOverview,
                topChallenge,
                false
        );
    }

    @Nullable
    private HomeUiModel.ChallengeSnippet buildTopChallengeSnippet(
            long rangeFrom, long rangeToExclusive, List<TransactionEntity> monthTx) {
        String uid = uidOrEmpty();
        List<ChallengeEntity> active = challengeDao.getActiveForUser(uid);
        if (active.isEmpty()) {
            return null;
        }
        long lastFrom = BudgetMonthUtils.monthStartMillisForOffset(-1);
        long lastTo = BudgetMonthUtils.monthEndExclusiveMillisForOffset(-1);
        List<TransactionEntity> lastMonthTx = transactionRepository.getBetween(uid, lastFrom, lastTo);
        long now = System.currentTimeMillis();
        List<TransactionEntity> weekTx = transactionRepository.getBetween(uid, now - 7L * 86_400_000L, now + 1);
        String monthKey = BudgetMonthUtils.monthKeyForOffset(0);
        List<BudgetEntity> budgets = budgetRepository.getForMonth(uid, monthKey);

        for (ChallengeEngine.Progress p : ChallengeEngine.evaluate(
                getApplication(),
                0,
                uid,
                active,
                monthTx,
                lastMonthTx,
                weekTx,
                budgets,
                transactionRepository,
                rangeFrom,
                rangeToExclusive)) {
            if (ChallengeEngine.Type.NO_SPEND_DAY.name().equals(p.type)) {
                for (ChallengeEntity ce : active) {
                    if (ce.id == p.challengeId && p.currentStreak > ce.bestStreak) {
                        ce.bestStreak = p.currentStreak;
                        challengeDao.update(ce);
                        break;
                    }
                }
            }
        }
        active = challengeDao.getActiveForUser(uid);
        List<ChallengeEngine.Progress> prog = ChallengeEngine.evaluate(
                getApplication(),
                0,
                uid,
                active,
                monthTx,
                lastMonthTx,
                weekTx,
                budgets,
                transactionRepository,
                rangeFrom,
                rangeToExclusive);
        if (prog.isEmpty()) {
            return null;
        }
        ChallengeEngine.Progress best = prog.get(0);
        for (int i = 1; i < prog.size(); i++) {
            ChallengeEngine.Progress c = prog.get(i);
            if (c.progressPct > best.progressPct
                    || (c.progressPct == best.progressPct && c.challengeId > best.challengeId)) {
                best = c;
            }
        }
        return new HomeUiModel.ChallengeSnippet(
                best.emoji, best.title, best.subtitle, best.progressPct);
    }

    private HomeUiModel.HomeBudgetOverview buildBudgetOverview(long rangeFrom, long rangeToExclusive) {
        String uid = uidOrEmpty();
        String monthKey = BudgetMonthUtils.monthKeyForOffset(0);
        List<BudgetEntity> budgets = budgetRepository.getForMonth(uid, monthKey);
        if (budgets.isEmpty()) {
            return new HomeUiModel.HomeBudgetOverview(
                    true,
                    getApplication().getString(R.string.home_budget_empty_primary),
                    getApplication().getString(R.string.home_budget_empty_secondary),
                    0,
                    0,
                    0
            );
        }
        double totalLimit = 0;
        double totalSpent = 0;
        int atRisk = 0;
        for (BudgetEntity b : budgets) {
            totalLimit += b.limitAmount;
            double spent = transactionRepository.sumBudgetOutgoingForCategoryBetween(
                    uid, b.categoryId, rangeFrom, rangeToExclusive);
            totalSpent += spent;
            if (b.limitAmount > 0) {
                double ratio = spent / b.limitAmount;
                if (ratio >= 0.8) {
                    atRisk++;
                }
            }
        }
        int pct = totalLimit > 0
                ? (int) Math.min(100, Math.round(totalSpent * 100.0 / totalLimit))
                : 0;
        boolean over = totalLimit > 0 && totalSpent > totalLimit;
        boolean warn = !over && totalLimit > 0 && totalSpent / totalLimit >= 0.8;
        int tone = over ? 2 : (warn ? 1 : 0);

        String spentShort = MoneyUtils.formatShortMillion(totalSpent);
        String limitShort = MoneyUtils.formatShortMillion(totalLimit);
        String primary = getApplication().getString(R.string.home_budget_used_pct, pct);
        String secondary = getApplication().getString(R.string.home_budget_sub_spent_limit, spentShort, limitShort);
        if (atRisk > 0) {
            secondary = secondary + "\n" + getApplication().getString(R.string.home_budget_at_risk, atRisk);
        }
        return new HomeUiModel.HomeBudgetOverview(false, primary, secondary, pct, tone, atRisk);
    }

    private List<ChartPoint> buildMonthSpendTrend(Calendar monthStart, List<TransactionEntity> monthTx) {
        int dim = monthStart.getActualMaximum(Calendar.DAY_OF_MONTH);
        double[] daily = new double[dim + 1];
        Calendar cal = Calendar.getInstance();
        for (TransactionEntity t : monthTx) {
            TransactionType type = safeType(t.type);
            if (type != TransactionType.EXPENSE && type != TransactionType.BORROW) {
                continue;
            }
            cal.setTimeInMillis(t.occurredAt);
            int dom = cal.get(Calendar.DAY_OF_MONTH);
            if (dom >= 1 && dom <= dim) {
                daily[dom] += t.amount;
            }
        }
        Calendar today = Calendar.getInstance();
        int lastDay = dim;
        if (monthStart.get(Calendar.YEAR) == today.get(Calendar.YEAR)
                && monthStart.get(Calendar.MONTH) == today.get(Calendar.MONTH)) {
            lastDay = Math.min(dim, today.get(Calendar.DAY_OF_MONTH));
        }
        List<ChartPoint> trend = new ArrayList<>();
        double cum = 0;
        for (int d = 1; d <= lastDay; d++) {
            cum += daily[d];
            trend.add(new ChartPoint(d, (float) cum));
        }
        return trend;
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
