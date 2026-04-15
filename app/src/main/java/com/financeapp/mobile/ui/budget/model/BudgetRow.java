package com.financeapp.mobile.ui.budget.model;

public class BudgetRow {

    public final String icon;
    public final int iconBgColorArgb;
    public final String title;
    public final String limitLine;
    public final String remainingLine;
    public final int progress;
    public final boolean overBudget;
    public final int remainingColorArgb;
    public final boolean showTodayTag;
    public final long budgetId;
    public final long categoryId;
    public final String spentLine;
    /** true khi đã chi ≥ 80% hạn mức — hiển thị badge cảnh báo. */
    public final boolean hasWarning;

    public BudgetRow(String icon, int iconBgColorArgb, String title, String limitLine,
                     String remainingLine, int progress, boolean overBudget,
                     int remainingColorArgb, boolean showTodayTag,
                     long budgetId, long categoryId, String spentLine, boolean hasWarning) {
        this.icon = icon;
        this.iconBgColorArgb = iconBgColorArgb;
        this.title = title;
        this.limitLine = limitLine;
        this.remainingLine = remainingLine;
        this.progress = progress;
        this.overBudget = overBudget;
        this.remainingColorArgb = remainingColorArgb;
        this.showTodayTag = showTodayTag;
        this.budgetId = budgetId;
        this.categoryId = categoryId;
        this.spentLine = spentLine;
        this.hasWarning = hasWarning;
    }
}
