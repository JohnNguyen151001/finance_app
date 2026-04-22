package com.financeapp.mobile.ui.chart;

/** Điểm (x, y) cho Line/Bar chart — x thường là ngày trong tháng (1..31). */
public class ChartPoint {

    public final float x;
    public final float y;

    public ChartPoint(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
