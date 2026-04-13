package com.financeapp.mobile.ui.report;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.financeapp.mobile.databinding.FragmentReportBinding;
import com.financeapp.mobile.ui.chart.ChartUiHelper;

public class ReportFragment extends Fragment {

    private FragmentReportBinding binding;
    private ReportViewModel viewModel;
    private boolean chartsStyled;

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

        binding.toolbarReport.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp());

        binding.textReportCategory.setOnClickListener(v ->
                Toast.makeText(requireContext(), com.financeapp.mobile.R.string.report_category_hint,
                        Toast.LENGTH_SHORT).show());

        viewModel.getUi().observe(getViewLifecycleOwner(), this::render);
    }

    private void render(ReportUiModel model) {
        if (binding == null || model == null) {
            return;
        }
        if (!chartsStyled) {
            ChartUiHelper.styleSpendLineChart(binding.chartReportLine);
            ChartUiHelper.styleSpendBarChart(binding.chartReportBar);
            chartsStyled = true;
        }
        binding.textReportCategory.setText(model.categoryTitle);
        binding.textReportTotal.setText(model.totalText);
        ChartUiHelper.bindLineChart(binding.chartReportLine, model.lineCumulative);
        ChartUiHelper.bindBarChart(binding.chartReportBar, model.barDaily);
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
        chartsStyled = false;
    }
}
