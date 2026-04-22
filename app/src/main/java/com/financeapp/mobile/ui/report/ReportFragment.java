package com.financeapp.mobile.ui.report;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeapp.mobile.databinding.FragmentReportBinding;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;
    private ReportViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentReportBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(ReportViewModel.class);

        setupCharts();
        setupTabs();

        binding.btnReportClose.setOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        viewModel.getUi().observe(getViewLifecycleOwner(), this::render);
    }

    private void setupTabs() {
        binding.tabsPeriod.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int offset = 0;
                switch (tab.getPosition()) {
                    case 0: offset = -1; break; // THÁNG TRƯỚC
                    case 1: offset = 0; break;  // THÁNG NÀY
                    case 2: offset = 1; break;  // TƯƠNG LAI
                }
                viewModel.setMonthOffset(offset);
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        // Chọn mặc định là "Tháng này"
        TabLayout.Tab thisMonth = binding.tabsPeriod.getTabAt(1);
        if (thisMonth != null) {
            thisMonth.select();
        }
    }

    private void setupCharts() {
        stylePieChart(binding.chartIncDonut);
        stylePieChart(binding.chartExpDonut);
    }

    private void stylePieChart(PieChart chart) {
        chart.setUsePercentValues(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(25, 5, 25, 5); // Tăng offset để hiển thị nhãn bên ngoài
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleRadius(61f);
        chart.setHoleRadius(70f);
        chart.setDrawCenterText(false);
        chart.setRotationAngle(0);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.getLegend().setEnabled(false);
        chart.setDrawEntryLabels(false); // Ẩn nhãn mặc định để vẽ nhãn ngoài
    }

    private void render(ReportUiModel model) {
        if (binding == null || model == null) {
            return;
        }

        binding.textTotalBalHeader.setText(model.closingBalance);
        binding.textOpeningVal.setText(model.openingBalance);
        binding.textClosingVal.setText(model.closingBalance);
        binding.textNetIncomeDisplay.setText(model.netIncome);
        binding.textIncSum.setText(model.incomeTotal);
        binding.textExpSum.setText(model.expenseTotal);

        binding.textGroupIncVal.setText(model.incomeTotal);
        binding.textGroupExpVal.setText(model.expenseTotal);

        double total = model.incomeValue + model.expenseValue;
        if (total > 0) {
            binding.barIncRatio.setProgress((int) (model.incomeValue * 100 / total));
            binding.barExpRatio.setProgress((int) (model.expenseValue * 100 / total));
        } else {
            binding.barIncRatio.setProgress(0);
            binding.barExpRatio.setProgress(0);
        }

        updatePieChart(binding.chartIncDonut, model.incomeShares);
        updatePieChart(binding.chartExpDonut, model.expenseShares);

        binding.rowDebtSummary.rowLabel.setText("Nợ");
        binding.rowDebtSummary.rowValue.setText(model.debtTotal);
        
        binding.rowLoanSummary.rowLabel.setText("Cho vay");
        binding.rowLoanSummary.rowValue.setText(model.loanTotal);
        
        binding.rowOthersSummary.rowLabel.setText("Khác");
        binding.rowOthersSummary.rowValue.setText(model.otherTotal);
    }

    private void updatePieChart(PieChart chart, List<ReportUiModel.CategoryShare> shares) {
        List<PieEntry> entries = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        if (shares.isEmpty()) {
            entries.add(new PieEntry(100f, ""));
            colors.add(Color.parseColor("#EEEEEE"));
        } else {
            for (ReportUiModel.CategoryShare share : shares) {
                entries.add(new PieEntry((float) share.amount, share.name));
                colors.add(share.color);
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        dataSet.setSelectionShift(3f);

        if (!shares.isEmpty()) {
            // Cấu hình nhãn bên ngoài
            dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
            dataSet.setValueLinePart1OffsetPercentage(80.f);
            dataSet.setValueLinePart1Length(0.4f);
            dataSet.setValueLinePart2Length(0.2f);
            dataSet.setValueLineColor(Color.LTGRAY);
            dataSet.setValueTextSize(9f);
            dataSet.setDrawValues(true);
        } else {
            dataSet.setDrawValues(false);
        }

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new ValueFormatter() {
            @Override
            public String getPieLabel(float value, PieEntry entry) {
                if (shares.isEmpty() || entry == null || entry.getLabel() == null) return "";
                String label = entry.getLabel();
                if (label.length() > 8) label = label.substring(0, 6) + "..";
                return String.format(Locale.getDefault(), "%s %.1f%%", label, value);
            }
        });
        
        chart.setData(data);
        chart.setUsePercentValues(true);
        chart.invalidate();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
