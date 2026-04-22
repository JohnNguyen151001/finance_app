package com.financeapp.mobile.domain;

import java.util.Calendar;
import java.util.Locale;

/** Tháng theo offset (0 = tháng hiện tại) — dùng chung Home + BudgetViewModel. */
public final class BudgetMonthUtils {

    private BudgetMonthUtils() {}

    public static Calendar calendarForOffset(int monthOffset) {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, monthOffset);
        return c;
    }

    public static String monthKeyForOffset(int monthOffset) {
        Calendar cal = calendarForOffset(monthOffset);
        return String.format(Locale.US, "%04d-%02d",
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
    }

    public static long monthStartMillisForOffset(int monthOffset) {
        Calendar c = calendarForOffset(monthOffset);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    public static long monthEndExclusiveMillisForOffset(int monthOffset) {
        Calendar c = calendarForOffset(monthOffset);
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        c.add(Calendar.MONTH, 1);
        return c.getTimeInMillis();
    }

    public static String monthLabelForOffset(int monthOffset) {
        Calendar c = calendarForOffset(monthOffset);
        return "Tháng " + (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR);
    }

    /** Số ngày còn lại trong tháng đang xem (kể cả hôm nay); 0 nếu xem tháng quá khứ. */
    public static int daysLeftInViewedMonth(int monthOffset) {
        if (monthOffset < 0) {
            return 0;
        }
        Calendar c = calendarForOffset(monthOffset);
        int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        int today = c.get(Calendar.DAY_OF_MONTH);
        return Math.max(0, lastDay - today + 1);
    }
}
