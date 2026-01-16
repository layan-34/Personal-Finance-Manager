package edu.birzeit.a1220775_1221026_courseproject.ui.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import edu.birzeit.a1220775_1221026_courseproject.R;

public class ReportsFragment extends Fragment {

    private ReportsViewModel reportsViewModel;
    private RadioGroup radioGroupReportPeriod;
    private TextView tvReportContent;
    private TextView tvTotalIncome;
    private TextView tvTotalExpense;
    private TextView tvNetBalance;
    private android.widget.LinearLayout layoutTopCategories;
    private TextView tvNoData;
    private Button btnGenerate;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reports, container, false);

        reportsViewModel = new ViewModelProvider(this).get(ReportsViewModel.class);

        radioGroupReportPeriod = root.findViewById(R.id.radioGroupReportPeriod);
        tvReportContent = root.findViewById(R.id.tvReportContent);
        tvTotalIncome = root.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = root.findViewById(R.id.tvTotalExpense);
        tvNetBalance = root.findViewById(R.id.tvNetBalance);
        layoutTopCategories = root.findViewById(R.id.layoutTopCategories);
        tvNoData = root.findViewById(R.id.tvNoData);
        btnGenerate = root.findViewById(R.id.btnGenerateReport);

        setupRadioGroup();

        btnGenerate.setOnClickListener(v -> {
            String selectedPeriod = getSelectedPeriod();
            reportsViewModel.generateReport(selectedPeriod);
        });

        reportsViewModel.getReport().observe(getViewLifecycleOwner(), report -> {
            tvReportContent.setText(report);
        });

        reportsViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            updateSelectedPeriodLabel(date);
        });

        root.findViewById(R.id.tvSelectedPeriodLabel).setOnClickListener(v -> showDatePicker());

        reportsViewModel.getTotalIncome().observe(getViewLifecycleOwner(), income -> {
            tvTotalIncome.setText(String.format(java.util.Locale.getDefault(), "$%.2f", income));
        });

        reportsViewModel.getTotalExpense().observe(getViewLifecycleOwner(), expense -> {
            tvTotalExpense.setText(String.format(java.util.Locale.getDefault(), "$%.2f", expense));
        });

        reportsViewModel.getNetBalance().observe(getViewLifecycleOwner(), balance -> {
            tvNetBalance.setText(String.format(java.util.Locale.getDefault(), "$%.2f", balance));
        });

        reportsViewModel.getTopCategories().observe(getViewLifecycleOwner(), categories -> {
            layoutTopCategories.removeAllViews();
            if (categories == null || categories.isEmpty()) {
                layoutTopCategories.addView(tvNoData);
                tvNoData.setVisibility(View.VISIBLE);
            } else {
                tvNoData.setVisibility(View.GONE);
                for (ReportsViewModel.CategoryUsage cat : categories) {
                    android.widget.LinearLayout row = new android.widget.LinearLayout(getContext());
                    row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
                    row.setPadding(0, 8, 0, 8);

                    TextView nameView = new TextView(getContext());
                    nameView.setText(cat.name);
                    nameView.setTextSize(14);
                    nameView.setTextColor(getResources().getColor(android.R.color.black));
                    nameView.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
                            0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f));

                    TextView amountView = new TextView(getContext());
                    amountView.setText(String.format(java.util.Locale.getDefault(), "$%.2f", cat.total));
                    amountView.setTextSize(14);
                    amountView.setTypeface(null, android.graphics.Typeface.BOLD);
                    amountView.setTextColor(getResources().getColor(R.color.expense)); // Use expense color

                    row.addView(nameView);
                    row.addView(amountView);
                    layoutTopCategories.addView(row);
                }
            }
        });

        return root;
    }

    private void updateSelectedPeriodLabel(Long date) {
        if (date == null)
            return;
        java.text.SimpleDateFormat sdf;

        int checkedId = radioGroupReportPeriod.getCheckedRadioButtonId();
        if (checkedId == R.id.radioDaily) {
            sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        } else if (checkedId == R.id.radioWeekly) {
            sdf = new java.text.SimpleDateFormat("'Week of' MMM dd, yyyy", Locale.getDefault());
        } else if (checkedId == R.id.radioMonthly) {
            sdf = new java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        } else if (checkedId == R.id.radioYearly) {
            sdf = new java.text.SimpleDateFormat("'Year' yyyy", Locale.getDefault());
        } else {
            sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        }

        TextView tvLabel = getView().findViewById(R.id.tvSelectedPeriodLabel);
        if (tvLabel != null) {
            tvLabel.setText(sdf.format(new java.util.Date(date)));
        }
    }

    private void showDatePicker() {
        int checkedId = radioGroupReportPeriod.getCheckedRadioButtonId();

        if (checkedId == R.id.radioMonthly) {
            showMonthPicker();
            return;
        }

        Calendar cal = Calendar.getInstance();
        Long current = reportsViewModel.getSelectedDate().getValue();
        if (current != null)
            cal.setTimeInMillis(current);

        // Force 2026
        cal.set(Calendar.YEAR, 2026);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    reportsViewModel.setSelectedDate(selected.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void showMonthPicker() {
        String[] months = { "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December" };

        new android.app.AlertDialog.Builder(requireContext())
                .setTitle("Select Month")
                .setItems(months, (dialog, which) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, 2026);
                    selected.set(Calendar.MONTH, which);
                    selected.set(Calendar.DAY_OF_MONTH, 1);
                    selected.set(Calendar.HOUR_OF_DAY, 0);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    reportsViewModel.setSelectedDate(selected.getTimeInMillis());
                })
                .show();
    }

    private void setupRadioGroup() {
        if (radioGroupReportPeriod == null)
            return;

        // Default to Daily
        radioGroupReportPeriod.check(R.id.radioDaily);

        radioGroupReportPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            String selectedPeriod = getSelectedPeriod();
            reportsViewModel.generateReport(selectedPeriod);
            updateSelectedPeriodLabel(reportsViewModel.getSelectedDate().getValue());
        });

        // Initial report
        reportsViewModel.generateReport("Daily");
    }

    private String getSelectedPeriod() {
        int checkedId = radioGroupReportPeriod.getCheckedRadioButtonId();
        if (checkedId == R.id.radioDaily)
            return "Daily";
        else if (checkedId == R.id.radioWeekly)
            return "Weekly";
        else if (checkedId == R.id.radioMonthly)
            return "Monthly";
        else if (checkedId == R.id.radioYearly)
            return "Yearly";
        return "Monthly";
    }
}
