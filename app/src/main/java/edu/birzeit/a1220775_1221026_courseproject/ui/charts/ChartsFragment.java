package edu.birzeit.a1220775_1221026_courseproject.ui.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.ui.custom.SimpleBarChart;
import edu.birzeit.a1220775_1221026_courseproject.ui.custom.SimpleLineChart;
import edu.birzeit.a1220775_1221026_courseproject.ui.custom.SimplePieChart;

public class ChartsFragment extends Fragment {

    private ChartsViewModel chartsViewModel;
    private SimplePieChart pieChart;
    private SimpleBarChart barChart;
    private SimpleLineChart lineChart;
    private Spinner spinnerChartType;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_charts, container, false);

        chartsViewModel = new ViewModelProvider(this).get(ChartsViewModel.class);

        pieChart = root.findViewById(R.id.pieChart);
        barChart = root.findViewById(R.id.barChart);
        lineChart = root.findViewById(R.id.lineChart);
        spinnerChartType = root.findViewById(R.id.spinnerChartType);

        setupChartTypeSpinner();

        // Observe Data
        chartsViewModel.getCategoryPieData().observe(getViewLifecycleOwner(), data -> {
            if (spinnerChartType.getSelectedItemPosition() == 0)
                updatePieChart(data);
        });

        chartsViewModel.getIncomeExpensePieData().observe(getViewLifecycleOwner(), data -> {
            if (spinnerChartType.getSelectedItemPosition() == 1)
                updatePieChart(data);
        });

        chartsViewModel.getExpenseTrend().observe(getViewLifecycleOwner(), data -> {
            updateBarChart(data);
            updateLineChart(data);
        });

        return root;
    }

    private void setupChartTypeSpinner() {
        String[] types = {
                "Expenses by Category (Pie)",
                "Income vs Expense (Pie)",
                "Expense Trend (Bar)",
                "Expense Trend (Line)"
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item,
                types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChartType.setAdapter(adapter);

        spinnerChartType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pieChart.setVisibility(View.GONE);
                barChart.setVisibility(View.GONE);
                lineChart.setVisibility(View.GONE);

                switch (position) {
                    case 0: // Category Pie
                        pieChart.setVisibility(View.VISIBLE);
                        ChartsViewModel.PieChartData catData = chartsViewModel.getCategoryPieData().getValue();
                        updatePieChart(catData);
                        break;
                    case 1: // Inc/Exp Pie
                        pieChart.setVisibility(View.VISIBLE);
                        ChartsViewModel.PieChartData incExpData = chartsViewModel.getIncomeExpensePieData().getValue();
                        updatePieChart(incExpData);
                        break;
                    case 2: // Bar
                        barChart.setVisibility(View.VISIBLE);
                        break;
                    case 3: // Line
                        lineChart.setVisibility(View.VISIBLE);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void updatePieChart(ChartsViewModel.PieChartData data) {
        if (data == null || data.values == null || data.values.isEmpty()) {
            pieChart.clear();
            return;
        }
        pieChart.setData(data.values, data.colors, data.labels);
    }

    private void updateBarChart(List<androidx.core.util.Pair<String, Double>> dataList) {
        if (dataList == null)
            return;
        barChart.setData(dataList);
    }

    private void updateLineChart(List<androidx.core.util.Pair<String, Double>> dataList) {
        if (dataList == null)
            return;
        lineChart.setData(dataList);
    }
}
