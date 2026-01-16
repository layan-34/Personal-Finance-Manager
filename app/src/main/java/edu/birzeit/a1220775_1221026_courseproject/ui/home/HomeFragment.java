package edu.birzeit.a1220775_1221026_courseproject.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import edu.birzeit.a1220775_1221026_courseproject.ui.custom.SimpleBarChart;
import edu.birzeit.a1220775_1221026_courseproject.ui.custom.SimplePieChart;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.databinding.FragmentHomeBinding;
import edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;
import edu.birzeit.a1220775_1221026_courseproject.ui.TransactionsAdapter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private TransactionsAdapter adapter;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private RadioGroup radioGroupPeriod;
    private android.widget.Button btnViewCharts;
    private android.widget.Button btnViewReports;
    private Map<Integer, String> categoryCache = new HashMap<>();
    private String lastLoadedDefaultPeriod;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        categoryRepository = new CategoryRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Binding might not automatically pick up type change immediately if generated
        // class isn't rebuilt,
        // so we might need a clean build. But manually findViewById would be safer if
        // binding is stale.
        // Assuming binding works for now after rebuild.
        // If binding error occurs, we can use root.findViewById(R.id.radioGroupPeriod).
        try {
            radioGroupPeriod = root.findViewById(R.id.radioGroupPeriod);
        } catch (Exception e) {
            // Fallback if binding name mismatch or issue
        }

        btnViewCharts = binding.btnViewCharts;
        btnViewReports = binding.btnViewReports;

        btnViewCharts
                .setOnClickListener(v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_charts));

        btnViewReports.setOnClickListener(
                v -> androidx.navigation.Navigation.findNavController(v).navigate(R.id.nav_reports));

        setupRadioGroup();
        setupRecyclerView();

        homeViewModel.getTotalIncome().observe(getViewLifecycleOwner(),
                income -> binding.tvTotalIncome.setText(String.format(Locale.getDefault(), "$%.2f", income)));

        homeViewModel.getTotalExpense().observe(getViewLifecycleOwner(),
                expense -> binding.tvTotalExpense.setText(String.format(Locale.getDefault(), "$%.2f", expense)));

        homeViewModel.getBalance().observe(getViewLifecycleOwner(),
                balance -> binding.tvBalance.setText(String.format(Locale.getDefault(), "$%.2f", balance)));

        homeViewModel.getRecentTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.updateTransactions(transactions);
                binding.tvNoTransactions.setVisibility(View.GONE);
                binding.rvRecentTransactions.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoTransactions.setVisibility(View.VISIBLE);
                binding.rvRecentTransactions.setVisibility(View.GONE);
            }
        });

        homeViewModel.getSelectedDate().observe(getViewLifecycleOwner(), date -> {
            updateSelectedPeriodLabel(date);
        });

        binding.tvSelectedPeriodLabel.setOnClickListener(v -> showDatePicker());

        return root;
    }

    private void updateSelectedPeriodLabel(Long date) {
        if (date == null)
            return;
        java.text.SimpleDateFormat sdf;

        int checkedId = radioGroupPeriod.getCheckedRadioButtonId();
        if (checkedId == R.id.radioDaily) {
            sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        } else if (checkedId == R.id.radioWeekly) {
            sdf = new java.text.SimpleDateFormat("'Week of' MMM dd, yyyy", Locale.getDefault());
        } else if (checkedId == R.id.radioMonthly) {
            sdf = new java.text.SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        } else if (checkedId == R.id.radioCustom) {
            // For custom, if we have a range, maybe show it?
            // Actually HomeViewModel tracks custom range separately.
            sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            // We'll show the actual range if possible, but for now let's just use the
            // selected date format
        } else {
            sdf = new java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        }

        binding.tvSelectedPeriodLabel.setText(sdf.format(new java.util.Date(date)));
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        Long current = homeViewModel.getSelectedDate().getValue();
        if (current != null)
            cal.setTimeInMillis(current);

        // Always force 2026 for now as per previous context
        cal.set(Calendar.YEAR, 2026);

        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth, 0, 0, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    homeViewModel.setSelectedDate(selected.getTimeInMillis());
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void setupCharts() {
        // Charts moved to separate fragment
    }

    private void setupChartTypeSpinner() {
        // Removed
    }

    private void setupRadioGroup() {
        if (radioGroupPeriod == null)
            return;

        SharedPreferences prefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        String defaultPeriod = prefs.getString("default_period", "Daily");

        int radioId = R.id.radioDaily; // Default
        if (defaultPeriod.equals("Weekly"))
            radioId = R.id.radioWeekly;
        else if (defaultPeriod.equals("Monthly"))
            radioId = R.id.radioMonthly;

        lastLoadedDefaultPeriod = defaultPeriod;
        radioGroupPeriod.check(radioId);

        radioGroupPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            String selected = "Daily";
            boolean isPredefined = true;
            if (checkedId == R.id.radioWeekly) {
                selected = "Weekly";
            } else if (checkedId == R.id.radioMonthly) {
                selected = "Monthly";
            } else if (checkedId == R.id.radioCustom) {
                selected = "Custom";
                isPredefined = false;
            }

            if (isPredefined) {

                Calendar cal = Calendar.getInstance();
                cal.set(2026, Calendar.JANUARY, 16, 0, 0, 0);
                cal.set(Calendar.MILLISECOND, 0);
                homeViewModel.setSelectedDate(cal.getTimeInMillis());
                binding.tvSelectedPeriodLabel.setVisibility(View.GONE);
            } else {
                binding.tvSelectedPeriodLabel.setVisibility(View.VISIBLE);
                showCustomDateRangePicker();
            }

            homeViewModel.setPeriod(selected);
        });

        // Initial load
        String initialSelected = "Daily";
        if (radioId == R.id.radioWeekly)
            initialSelected = "Weekly";
        else if (radioId == R.id.radioMonthly)
            initialSelected = "Monthly";

        homeViewModel.setPeriod(initialSelected);
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = binding.rvRecentTransactions;
        adapter = new TransactionsAdapter(new ArrayList<>(), null);

        String userEmail = userRepository.getCurrentUser();
        List<Category> categories = categoryRepository.getCategoriesByUser(userEmail);
        if (categories != null) {
            for (Category cat : categories) {
                categoryCache.put(cat.getId(), cat.getName());
            }
        }
        adapter.setCategoryCache(categoryCache);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);
    }

    private void updatePieChart() {
        // Moved to ChartsFragment
    }

    private void showCustomDateRangePicker() {
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long start = cal.getTimeInMillis();
        cal.set(2026, Calendar.JANUARY, 15, 23, 59, 59); // Default to mid-month or today in 2026
        cal.set(Calendar.MILLISECOND, 999);
        long end = cal.getTimeInMillis();

        com.google.android.material.datepicker.MaterialDatePicker<androidx.core.util.Pair<Long, Long>> datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder
                .dateRangePicker()
                .setTitleText("Select Date Range")
                .setSelection(androidx.core.util.Pair.create(start, end))
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            Long startDate = selection.first;
            Long endDate = selection.second;
            if (startDate != null && endDate != null) {
                homeViewModel.setCustomDateRange(startDate, endDate);
            }
        });

        datePicker.show(getParentFragmentManager(), "date_range_picker");
    }

    private void updateBarChart() {
        // Moved to ChartsFragment
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check if default period changed while we were away (e.g. in Settings)
        if (getContext() != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
            String currentDefault = prefs.getString("default_period", "Monthly");

            if (lastLoadedDefaultPeriod != null && !lastLoadedDefaultPeriod.equals(currentDefault)) {
                lastLoadedDefaultPeriod = currentDefault;
                int radioId = R.id.radioMonthly; // Default
                if ("Daily".equals(currentDefault))
                    radioId = R.id.radioDaily;
                else if ("Weekly".equals(currentDefault))
                    radioId = R.id.radioWeekly;

                if (radioGroupPeriod != null) {
                    radioGroupPeriod.check(radioId);
                }
            } else {
                // Refresh data
                if (homeViewModel != null) {
                    homeViewModel.refresh();
                }
            }
        }
    }
}