package com.financeapp.mobile.ui.transaction;

import com.financeapp.mobile.ui.transaction.model.LedgerListItem;

import java.util.Collections;
import java.util.List;

public class LedgerUiModel {

    public final String expenseTotal;
    public final String incomeTotal;
    public final String balanceText;
    public final List<LedgerListItem> rows;

    public LedgerUiModel(String expenseTotal, String incomeTotal, String balanceText, List<LedgerListItem> rows) {
        this.expenseTotal = expenseTotal;
        this.incomeTotal = incomeTotal;
        this.balanceText = balanceText;
        this.rows = rows != null ? rows : Collections.emptyList();
    }
}
