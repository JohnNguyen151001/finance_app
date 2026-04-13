package com.financeapp.mobile.ui.transaction.model;

public class LedgerListItem {

    public static final int TYPE_HEADER = 0;
    public static final int TYPE_LINE = 1;

    public final int viewType;
    public final String headerText;
    public final String icon;
    public final String title;
    public final String subtitle;
    public final String amount;
    public final int amountColorArgb;

    public static LedgerListItem header(String headerText) {
        return new LedgerListItem(TYPE_HEADER, headerText, null, null, null, null, 0);
    }

    public static LedgerListItem line(String icon, String title, String subtitle, String amount, int amountColorArgb) {
        return new LedgerListItem(TYPE_LINE, null, icon, title, subtitle, amount, amountColorArgb);
    }

    private LedgerListItem(int viewType, String headerText, String icon, String title, String subtitle,
                           String amount, int amountColorArgb) {
        this.viewType = viewType;
        this.headerText = headerText;
        this.icon = icon;
        this.title = title;
        this.subtitle = subtitle;
        this.amount = amount;
        this.amountColorArgb = amountColorArgb;
    }
}
