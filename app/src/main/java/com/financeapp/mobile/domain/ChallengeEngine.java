package com.financeapp.mobile.domain;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.financeapp.mobile.R;
import com.financeapp.mobile.data.local.entity.BudgetEntity;
import com.financeapp.mobile.data.local.entity.ChallengeEntity;
import com.financeapp.mobile.data.local.entity.TransactionEntity;
import com.financeapp.mobile.data.repository.TransactionRepository;
import com.financeapp.mobile.domain.model.TransactionType;
import com.financeapp.mobile.ui.format.MoneyUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Tính tiến độ thử thách từ giao dịch + ngân sách — không ghi DB.
 */
public final class ChallengeEngine {

    private ChallengeEngine() {
    }

    public enum Type {
        NO_SPEND_DAY,
        BEAT_LAST_MONTH,
        WEEKLY_LIMIT,
        BUDGET_PERFECT
    }

    public static final class Progress {
        public final long challengeId;
        @NonNull
        public final String type;
        @NonNull
        public final String emoji;
        @NonNull
        public final String title;
        @NonNull
        public final String subtitle;
        /** 0–100: mức “áp lực” hoặc hoàn thành tùy loại. */
        public final int progressPct;
        public final boolean achieved;
        /** NO_SPEND_DAY: streak hiện tại (để cập nhật bestStreak). */
        public final int currentStreak;

        public Progress(long challengeId, @NonNull String type, @NonNull String emoji,
                        @NonNull String title, @NonNull String subtitle,
                        int progressPct, boolean achieved, int currentStreak) {
            this.challengeId = challengeId;
            this.type = type;
            this.emoji = emoji;
            this.title = title;
            this.subtitle = subtitle;
            this.progressPct = progressPct;
            this.achieved = achieved;
            this.currentStreak = currentStreak;
        }
    }

    @NonNull
    public static List<Progress> evaluate(
            @NonNull Application app,
            int monthOffset,
            @NonNull String userId,
            @NonNull List<ChallengeEntity> active,
            @NonNull List<TransactionEntity> thisMonthTx,
            @NonNull List<TransactionEntity> lastMonthTx,
            /** Giao dịch 7 ngày gần nhất (rolling) — WEEKLY_LIMIT. */
            @NonNull List<TransactionEntity> rollingWeekTx,
            @NonNull List<BudgetEntity> budgets,
            @NonNull TransactionRepository txRepo,
            long rangeFrom,
            long rangeToExclusive) {
        List<Progress> out = new ArrayList<>();
        ZoneId zone = ZoneId.systemDefault();
        for (ChallengeEntity e : active) {
            if (!e.active) {
                continue;
            }
            Type t = parseType(e.type);
            if (t == null) {
                continue;
            }
            Progress p = null;
            switch (t) {
                case NO_SPEND_DAY:
                    p = evalNoSpend(app, e, thisMonthTx, rangeFrom, rangeToExclusive, monthOffset, zone);
                    break;
                case BEAT_LAST_MONTH:
                    p = evalBeatLastMonth(app, e, thisMonthTx, lastMonthTx);
                    break;
                case WEEKLY_LIMIT:
                    p = evalWeeklyLimit(app, e, rollingWeekTx);
                    break;
                case BUDGET_PERFECT:
                    p = evalBudgetPerfect(app, e, budgets, txRepo, userId, rangeFrom, rangeToExclusive);
                    break;
                default:
                    break;
            }
            if (p != null) {
                out.add(p);
            }
        }
        return out;
    }

    @Nullable
    private static Type parseType(@Nullable String raw) {
        if (raw == null || raw.isEmpty()) {
            return null;
        }
        try {
            return Type.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private static boolean isOutgoing(@NonNull TransactionEntity t) {
        TransactionType ty = safeType(t.type);
        return ty == TransactionType.EXPENSE || ty == TransactionType.BORROW;
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

    private static LocalDate toLocalDate(long millis, ZoneId zone) {
        return Instant.ofEpochMilli(millis).atZone(zone).toLocalDate();
    }

    private static Progress evalNoSpend(
            Application app,
            ChallengeEntity e,
            List<TransactionEntity> thisMonthTx,
            long rangeFrom,
            long rangeToExclusive,
            int monthOffset,
            ZoneId zone) {
        Set<LocalDate> spendDays = new HashSet<>();
        for (TransactionEntity t : thisMonthTx) {
            if (!isOutgoing(t)) {
                continue;
            }
            if (t.transDate < rangeFrom || t.transDate >= rangeToExclusive) {
                continue;
            }
            spendDays.add(toLocalDate(t.transDate, zone));
        }
        LocalDate start = toLocalDate(rangeFrom, zone);
        LocalDate end = (monthOffset == 0)
                ? LocalDate.now(zone)
                : toLocalDate(rangeToExclusive - 1, zone);
        if (end.isBefore(start)) {
            end = start;
        }
        int streak = 0;
        LocalDate d = end;
        while (!d.isBefore(start)) {
            if (spendDays.contains(d)) {
                break;
            }
            streak++;
            d = d.minusDays(1);
        }
        boolean achieved = streak >= 1;
        int pct = Math.min(100, streak * 15);
        String sub = app.getString(R.string.challenge_no_spend_sub, streak, e.bestStreak);
        return new Progress(e.id, e.type, "\uD83D\uDD25",
                app.getString(R.string.challenge_no_spend_title),
                sub, pct, achieved, streak);
    }

    private static double sumOutgoing(List<TransactionEntity> txs) {
        double s = 0;
        for (TransactionEntity t : txs) {
            if (isOutgoing(t)) {
                s += t.amount;
            }
        }
        return s;
    }

    private static Progress evalBeatLastMonth(
            Application app,
            ChallengeEntity e,
            List<TransactionEntity> thisMonthTx,
            List<TransactionEntity> lastMonthTx) {
        double thisE = sumOutgoing(thisMonthTx);
        double lastE = sumOutgoing(lastMonthTx);
        boolean achieved = lastE <= 0 ? thisE <= 0 : thisE < lastE;
        int pct;
        if (lastE <= 0) {
            pct = thisE <= 0 ? 100 : 40;
        } else if (thisE <= lastE) {
            pct = 100;
        } else {
            pct = (int) Math.max(0, Math.min(99, Math.round(100.0 * lastE / thisE)));
        }
        String sub = achieved
                ? app.getString(R.string.challenge_beat_month_sub_winning,
                MoneyUtils.formatShortMillion(thisE), MoneyUtils.formatShortMillion(lastE))
                : app.getString(R.string.challenge_beat_month_sub_losing,
                MoneyUtils.formatShortMillion(thisE), MoneyUtils.formatShortMillion(lastE));
        return new Progress(e.id, e.type, "\uD83C\uDFAF",
                app.getString(R.string.challenge_beat_month_title),
                sub, pct, achieved, 0);
    }

    private static Progress evalWeeklyLimit(
            Application app,
            ChallengeEntity e,
            List<TransactionEntity> rollingWeekTx) {
        double spent = 0;
        for (TransactionEntity t : rollingWeekTx) {
            if (isOutgoing(t)) {
                spent += t.amount;
            }
        }
        double target = e.targetValue > 0 ? e.targetValue : 1;
        boolean achieved = spent <= target;
        int pct = (int) Math.min(100, Math.round(spent * 100.0 / target));
        String sub = app.getString(R.string.challenge_weekly_sub,
                MoneyUtils.formatVnd(spent), MoneyUtils.formatVnd(target));
        return new Progress(e.id, e.type, "\uD83D\uDCAA",
                app.getString(R.string.challenge_weekly_title),
                sub, pct, achieved, 0);
    }

    private static Progress evalBudgetPerfect(
            Application app,
            ChallengeEntity e,
            List<BudgetEntity> budgets,
            TransactionRepository txRepo,
            @NonNull String userId,
            long rangeFrom,
            long rangeToExclusive) {
        if (budgets.isEmpty()) {
            return new Progress(e.id, e.type, "\uD83C\uDFC6",
                    app.getString(R.string.challenge_budget_perfect_title),
                    app.getString(R.string.challenge_budget_perfect_sub_empty),
                    100, true, 0);
        }
        int over = 0;
        for (BudgetEntity b : budgets) {
            double spent = txRepo.sumExpenseForCategoryBetween(
                    userId, b.categoryId, rangeFrom, rangeToExclusive);
            if (b.limitAmount > 0 && spent > b.limitAmount) {
                over++;
            }
        }
        int total = budgets.size();
        int ok = total - over;
        int pct = (int) Math.round(ok * 100.0 / total);
        boolean achieved = over == 0;
        String sub = app.getString(R.string.challenge_budget_perfect_sub, ok, total);
        return new Progress(e.id, e.type, "\uD83C\uDFC6",
                app.getString(R.string.challenge_budget_perfect_title),
                sub, pct, achieved, 0);
    }
}
