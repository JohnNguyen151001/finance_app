package com.financeapp.mobile.ui.format;

import java.text.NumberFormat;
import java.util.Locale;

public final class MoneyUtils {

    private MoneyUtils() {
    }

    public static String formatVnd(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
        return nf.format(Math.round(amount)) + " đ";
    }

    /** Hiển thị số dư có dấu âm theo Figma (vd: -548,000 đ). */
    public static String formatSignedBalance(double amount) {
        if (amount < 0) {
            return "-" + formatVnd(-amount);
        }
        return formatVnd(amount);
    }

    public static String formatSignedExpense(double amount) {
        return "-" + formatVnd(amount);
    }

    public static String formatIncome(double amount) {
        return formatVnd(amount);
    }

    /** Định dạng kiểu Figma: 1,800,000.00 (dấu phẩy ngăn cách nghìn). */
    public static String formatAmountDotDecimal(double amount) {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        nf.setMinimumFractionDigits(2);
        nf.setMaximumFractionDigits(2);
        return nf.format(amount);
    }

    /** Rút gọn triệu: 1.8 M khi >= 1_000_000. */
    public static String formatShortMillion(double amount) {
        if (amount >= 1_000_000d) {
            return String.format(Locale.US, "%.1f M", amount / 1_000_000d);
        }
        return formatAmountDotDecimal(amount);
    }
}
