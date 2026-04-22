package com.financeapp.mobile.ui.format;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateDisplayUtils {

    private DateDisplayUtils() {
    }

    public static String formatTransactionSubtitle(long occurredAt) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long startToday = cal.getTimeInMillis();
        long startYesterday = startToday - 86_400_000L;

        if (occurredAt >= startToday) {
            return "Hôm nay";
        }
        if (occurredAt >= startYesterday) {
            return "Hôm qua";
        }
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));
        return df.format(new Date(occurredAt));
    }

    public static String formatFullDate(long millis) {
        SimpleDateFormat df = new SimpleDateFormat("EEEE, dd/MM/yyyy", new Locale("vi", "VN"));
        return df.format(new Date(millis));
    }
}
