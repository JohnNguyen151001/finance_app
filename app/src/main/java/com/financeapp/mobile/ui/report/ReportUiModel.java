package com.financeapp.mobile.ui.report;

import com.financeapp.mobile.ui.chart.ChartPoint;

import java.util.Collections;
import java.util.List;

public class ReportUiModel {

    public final String categoryTitle;
    public final String totalText;
    public final List<ChartPoint> lineCumulative;
    public final List<ChartPoint> barDaily;

    public ReportUiModel(String categoryTitle, String totalText,
                         List<ChartPoint> lineCumulative, List<ChartPoint> barDaily) {
        this.categoryTitle = categoryTitle;
        this.totalText = totalText;
        this.lineCumulative = lineCumulative != null ? lineCumulative : Collections.emptyList();
        this.barDaily = barDaily != null ? barDaily : Collections.emptyList();
    }
}
