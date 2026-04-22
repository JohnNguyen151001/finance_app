package com.financeapp.mobile.ui.report;

import com.financeapp.mobile.ui.chart.ChartPoint;
import java.util.Collections;
import java.util.List;

public class ReportUiModel {
    public final String openingBalance;
    public final String closingBalance;
    public final String incomeTotal;
    public final String expenseTotal;
    public final String netIncome;
    public final double netIncomeValue;
    public final double incomeValue;
    public final double expenseValue;

    public final String debtTotal;
    public final String loanTotal;
    public final String otherTotal;

    public final List<CategoryShare> incomeShares;
    public final List<CategoryShare> expenseShares;

    public ReportUiModel(String openingBalance, String closingBalance, 
                         String incomeTotal, String expenseTotal, String netIncome, 
                         double netIncomeValue, double incomeValue, double expenseValue,
                         String debtTotal, String loanTotal, String otherTotal,
                         List<CategoryShare> incomeShares, List<CategoryShare> expenseShares) {
        this.openingBalance = openingBalance;
        this.closingBalance = closingBalance;
        this.incomeTotal = incomeTotal;
        this.expenseTotal = expenseTotal;
        this.netIncome = netIncome;
        this.netIncomeValue = netIncomeValue;
        this.incomeValue = incomeValue;
        this.expenseValue = expenseValue;
        this.debtTotal = debtTotal;
        this.loanTotal = loanTotal;
        this.otherTotal = otherTotal;
        this.incomeShares = incomeShares != null ? incomeShares : Collections.emptyList();
        this.expenseShares = expenseShares != null ? expenseShares : Collections.emptyList();
    }

    public static class CategoryShare {
        public final String name;
        public final double amount;
        public final float percentage;
        public final int color;

        public CategoryShare(String name, double amount, float percentage, int color) {
            this.name = name;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }
    }
}
