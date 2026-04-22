package com.financeapp.mobile.ui.home.model;

import androidx.annotation.Nullable;

import com.financeapp.mobile.ui.chart.ChartPoint;

import java.util.Collections;
import java.util.List;

public class HomeUiModel {

    public final String balanceText;
    public final String walletName;
    public final String walletBalanceText;
    public final String monthExpenseText;
    public final String monthIncomeText;
    public final List<TopSpendingRow> topSpending;
    public final List<RecentRow> recent;
    /** Luỹ kế chi trong tháng theo ngày (trục x = ngày). */
    public final List<ChartPoint> monthSpendTrend;
    /** Tab Chi tiêu nhiều nhất: true = 7 ngày trong tháng, false = cả tháng. */
    public final boolean topSpendingWeekly;
    public final boolean budgetLoading;
    @Nullable
    public final HomeBudgetOverview budgetOverview;
    @Nullable
    public final ChallengeSnippet topChallenge;

    public HomeUiModel(String balanceText, String walletName, String walletBalanceText,
                       String monthExpenseText, String monthIncomeText,
                       List<TopSpendingRow> topSpending, List<RecentRow> recent,
                       List<ChartPoint> monthSpendTrend,
                       boolean topSpendingWeekly,
                       @Nullable HomeBudgetOverview budgetOverview,
                       @Nullable ChallengeSnippet topChallenge,
                       boolean budgetLoading) {
        this.balanceText = balanceText;
        this.walletName = walletName;
        this.walletBalanceText = walletBalanceText;
        this.monthExpenseText = monthExpenseText;
        this.monthIncomeText = monthIncomeText;
        this.topSpending = topSpending != null ? topSpending : Collections.emptyList();
        this.recent = recent != null ? recent : Collections.emptyList();
        this.monthSpendTrend = monthSpendTrend != null ? monthSpendTrend : Collections.emptyList();
        this.topSpendingWeekly = topSpendingWeekly;
        this.budgetOverview = budgetOverview;
        this.topChallenge = topChallenge;
        this.budgetLoading = budgetLoading;
    }

    public HomeUiModel withBudgetLoading(boolean loading) {
        return new HomeUiModel(
                balanceText, walletName, walletBalanceText,
                monthExpenseText, monthIncomeText,
                topSpending, recent, monthSpendTrend,
                topSpendingWeekly,
                budgetOverview,
                topChallenge,
                loading
        );
    }

    /** Tóm tắt thử thách nổi bật cho widget Tổng quan. */
    public static final class ChallengeSnippet {
        public final String emoji;
        public final String title;
        public final String subtitle;
        public final int progressPct;

        public ChallengeSnippet(String emoji, String title, String subtitle, int progressPct) {
            this.emoji = emoji;
            this.title = title;
            this.subtitle = subtitle;
            this.progressPct = progressPct;
        }
    }

    /** 0 = ổn, 1 = gần hạn (≥80% tổng), 2 = vượt tổng hạn mức. */
    public static final class HomeBudgetOverview {
        public final boolean empty;
        public final String primaryLine;
        public final String secondaryLine;
        public final int progressPct;
        public final int healthTone;
        public final int atRiskCount;

        public HomeBudgetOverview(boolean empty, String primaryLine, String secondaryLine,
                                  int progressPct, int healthTone, int atRiskCount) {
            this.empty = empty;
            this.primaryLine = primaryLine;
            this.secondaryLine = secondaryLine;
            this.progressPct = progressPct;
            this.healthTone = healthTone;
            this.atRiskCount = atRiskCount;
        }
    }

    public static class TopSpendingRow {
        public final String icon;
        public final String name;
        public final int percent;
        public final int progress;

        public TopSpendingRow(String icon, String name, int percent, int progress) {
            this.icon = icon;
            this.name = name;
            this.percent = percent;
            this.progress = progress;
        }
    }

    public static class RecentRow {
        public final String icon;
        public final String title;
        public final String subtitle;
        public final String amount;
        /** Màu chữ đã resolve (ARGB), không phải resource id. */
        public final int amountColorArgb;

        public RecentRow(String icon, String title, String subtitle, String amount, int amountColorArgb) {
            this.icon = icon;
            this.title = title;
            this.subtitle = subtitle;
            this.amount = amount;
            this.amountColorArgb = amountColorArgb;
        }
    }
}
