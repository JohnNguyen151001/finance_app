package com.financeapp.mobile.ui.budget.model;

/** Tổng quan thẻ gauge + 3 cột thống kê (màn Ngân sách Figma). */
public class BudgetSummary {

    public final String canSpendFormatted;
    public final String totalBudgetShort;
    public final String totalSpentFormatted;
    public final String daysLeftLabel;
    /** 0..1 — tỷ lệ đã chi / tổng hạn mức (0 nếu không có ngân sách). */
    public final float gaugeSpendRatio;

    public BudgetSummary(String canSpendFormatted, String totalBudgetShort, String totalSpentFormatted,
                         String daysLeftLabel, float gaugeSpendRatio) {
        this.canSpendFormatted = canSpendFormatted;
        this.totalBudgetShort = totalBudgetShort;
        this.totalSpentFormatted = totalSpentFormatted;
        this.daysLeftLabel = daysLeftLabel;
        this.gaugeSpendRatio = gaugeSpendRatio;
    }
}
