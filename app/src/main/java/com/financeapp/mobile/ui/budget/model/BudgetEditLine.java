package com.financeapp.mobile.ui.budget.model;

/**
 * Một dòng nhập hạn mức trong luồng tạo nhiều ngân sách cùng lúc.
 */
public class BudgetEditLine {

    public final long categoryId;
    public final String name;
    public final String iconEmoji;
    /** Hạn mức đã có (để điền sẵn khi sửa / đã tạo trước đó). */
    public final double prefilledLimit;

    public BudgetEditLine(long categoryId, String name, String iconEmoji, double prefilledLimit) {
        this.categoryId = categoryId;
        this.name = name != null ? name : "?";
        this.iconEmoji = iconEmoji != null ? iconEmoji : "📁";
        this.prefilledLimit = prefilledLimit;
    }
}
