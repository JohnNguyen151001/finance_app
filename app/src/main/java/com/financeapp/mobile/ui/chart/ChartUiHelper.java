package com.financeapp.mobile.ui.chart;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.financeapp.mobile.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.List;

public final class ChartUiHelper {

    private ChartUiHelper() {
    }

    public static void styleSpendLineChart(@NonNull LineChart chart) {
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setPinchZoom(false);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraOffsets(8f, 8f, 8f, 8f);
        chart.setNoDataText("");

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setTextColor(ContextCompat.getColor(chart.getContext(), R.color.text_secondary));
        x.setTextSize(10f);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(chart.getContext(), R.color.divider));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(chart.getContext(), R.color.text_secondary));
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisRight().setEnabled(false);
    }

    public static void styleSpendBarChart(@NonNull BarChart chart) {
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(false);
        chart.setFitBars(true);
        chart.getDescription().setEnabled(false);
        chart.getLegend().setEnabled(false);
        chart.setExtraOffsets(8f, 8f, 8f, 16f);
        chart.setNoDataText("");

        XAxis x = chart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setDrawGridLines(false);
        x.setGranularity(1f);
        x.setTextColor(ContextCompat.getColor(chart.getContext(), R.color.text_secondary));
        x.setTextSize(10f);

        chart.getAxisLeft().setDrawGridLines(true);
        chart.getAxisLeft().setAxisMinimum(0f);
        chart.getAxisLeft().setGridColor(ContextCompat.getColor(chart.getContext(), R.color.divider));
        chart.getAxisLeft().setTextColor(ContextCompat.getColor(chart.getContext(), R.color.text_secondary));
        chart.getAxisLeft().setTextSize(10f);
        chart.getAxisRight().setEnabled(false);
    }

    public static void bindLineChart(@NonNull LineChart chart, @NonNull List<ChartPoint> points) {
        Context ctx = chart.getContext();
        List<Entry> entries = new ArrayList<>();
        for (ChartPoint p : points) {
            entries.add(new Entry(p.x, p.y));
        }
        if (entries.isEmpty()) {
            entries.add(new Entry(1f, 0f));
            entries.add(new Entry(2f, 0f));
        } else if (entries.size() == 1) {
            Entry e = entries.get(0);
            entries.add(new Entry(e.getX() + 0.5f, e.getY()));
        }
        LineDataSet set = new LineDataSet(entries, "");
        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        set.setCubicIntensity(0.2f);
        set.setColor(ContextCompat.getColor(ctx, R.color.spend_green));
        set.setLineWidth(2.2f);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setFillColor(ContextCompat.getColor(ctx, R.color.spend_green_light));
        set.setFillAlpha(60);
        set.setDrawFilled(true);

        LineData data = new LineData(set);
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int d = Math.round(value);
                return d > 0 ? String.valueOf(d) : "";
            }
        });
        if (!entries.isEmpty()) {
            float maxX = entries.get(entries.size() - 1).getX();
            chart.getXAxis().setAxisMinimum(0.5f);
            chart.getXAxis().setAxisMaximum(Math.max(2f, maxX + 0.5f));
        }
        chart.invalidate();
    }

    public static void bindBarChart(@NonNull BarChart chart, @NonNull List<ChartPoint> points) {
        Context ctx = chart.getContext();
        List<BarEntry> entries = new ArrayList<>();
        for (ChartPoint p : points) {
            entries.add(new BarEntry(p.x, p.y));
        }
        if (entries.isEmpty()) {
            entries.add(new BarEntry(1f, 0f));
            entries.add(new BarEntry(2f, 0f));
        } else if (entries.size() == 1) {
            BarEntry e = entries.get(0);
            entries.add(new BarEntry(e.getX() + 0.5f, e.getY()));
        }
        BarDataSet set = new BarDataSet(entries, "");
        set.setColor(ContextCompat.getColor(ctx, R.color.expense_red));
        set.setDrawValues(false);

        BarData data = new BarData(set);
        data.setBarWidth(0.6f);
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int d = Math.round(value);
                return d > 0 ? String.valueOf(d) : "";
            }
        });
        if (!entries.isEmpty()) {
            float maxX = entries.get(entries.size() - 1).getX();
            chart.getXAxis().setAxisMinimum(0.5f);
            chart.getXAxis().setAxisMaximum(Math.max(2f, maxX + 0.5f));
        }
        chart.invalidate();
    }
}
