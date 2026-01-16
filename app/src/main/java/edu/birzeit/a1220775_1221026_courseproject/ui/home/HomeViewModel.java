package edu.birzeit.a1220775_1221026_courseproject.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;
import edu.birzeit.a1220775_1221026_courseproject.repository.TransactionRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class HomeViewModel extends AndroidViewModel {
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private MutableLiveData<Double> totalIncome = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> totalExpense = new MutableLiveData<>(0.0);
    private MutableLiveData<Double> balance = new MutableLiveData<>(0.0);
    private MutableLiveData<List<Transaction>> recentTransactions = new MutableLiveData<>();

    private MutableLiveData<Map<Integer, Double>> categoryExpenses = new MutableLiveData<>();
    private MutableLiveData<Map<Integer, Double>> categoryIncome = new MutableLiveData<>();
    private MutableLiveData<List<androidx.core.util.Pair<String, Double>>> expenseTrend = new MutableLiveData<>();

    private String currentPeriod = "Daily"; // Default to Daily (Today)
    private long customStartDate = 0;
    private long customEndDate = 0;
    private MutableLiveData<Long> selectedDate = new MutableLiveData<>();

    public HomeViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        userRepository = new UserRepository(application);

        // Initialize selectedDate to Today (Jan 16, 2026 as per user requirement)
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JANUARY, 16, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        selectedDate.setValue(cal.getTimeInMillis());

        // Initialize custom range to Jan 2026 by default
        cal.set(2026, Calendar.JANUARY, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        customStartDate = cal.getTimeInMillis();
        cal.set(2026, Calendar.JANUARY, 31, 23, 59, 59);
        cal.set(Calendar.MILLISECOND, 999);
        customEndDate = cal.getTimeInMillis();
    }

    public LiveData<Long> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
        loadDashboardData();
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<Double> getBalance() {
        return balance;
    }

    public LiveData<List<Transaction>> getRecentTransactions() {
        return recentTransactions;
    }

    public LiveData<Map<Integer, Double>> getCategoryExpenses() {
        return categoryExpenses;
    }

    public LiveData<Map<Integer, Double>> getCategoryIncome() {
        return categoryIncome;
    }

    public LiveData<List<androidx.core.util.Pair<String, Double>>> getExpenseTrend() {
        return expenseTrend;
    }

    public void setCustomDateRange(long start, long end) {
        this.customStartDate = start;
        this.customEndDate = end;
        if ("Custom".equals(currentPeriod)) { // Only reload if currently in Custom mode or switching to it?
            // Actually setPeriod calls loadDashboardData.
            // If we just set range, we might want to refresh if period is already Custom.
            loadDashboardData();
        }
    }

    public void setPeriod(String period) {
        this.currentPeriod = period;
        loadDashboardData();
    }

    public String getSelectedPeriod() {
        return currentPeriod;
    }

    public void loadDashboardData() {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty())
            return;

        long[] dateRange = calculateDateRange(currentPeriod);
        long startDate = dateRange[0];
        long endDate = dateRange[1];

        // 1. Load Totals
        Double income = 0.0;
        Double expense = 0.0;

        if (currentPeriod.equals("All")) {
            income = transactionRepository.getTotalByUserAndType(userEmail, "INCOME");
            expense = transactionRepository.getTotalByUserAndType(userEmail, "EXPENSE");
        } else {
            income = transactionRepository.getTotalByUserTypeAndDateRange(userEmail, "INCOME", startDate, endDate);
            expense = transactionRepository.getTotalByUserTypeAndDateRange(userEmail, "EXPENSE", startDate, endDate);
        }

        totalIncome.setValue(income != null ? income : 0.0);
        totalExpense.setValue(expense != null ? expense : 0.0);
        balance.setValue((income != null ? income : 0.0) - (expense != null ? expense : 0.0));

        // 2. Load Transactions for List and Charts
        List<Transaction> transactions;
        if (currentPeriod.equals("All")) {
            transactions = transactionRepository.getTransactionsByUser(userEmail);
        } else {
            transactions = transactionRepository.getTransactionsByUserAndDateRange(userEmail, startDate, endDate);
        }

        // Recent Transactions (Limit to 10 for UI, but logic here fetches all for
        // charts - wait, optimization:
        // Recent List usually wants just 10. Charts want ALL expenses.
        // If I limit query to 10, charts will be wrong.
        // So I fetch all, then sublist for LiveData if needed, or separate LiveData.

        if (transactions != null) {
            // Update Recent List (limited)
            if (transactions.size() > 10) {
                recentTransactions.setValue(transactions.subList(0, 10));
            } else {
                recentTransactions.setValue(transactions);
            }

            // Update Category Expenses for Bar Chart
            Map<Integer, Double> catExpenses = new HashMap<>();
            Map<Integer, Double> catIncome = new HashMap<>();

            for (Transaction t : transactions) {
                if ("EXPENSE".equals(t.getType())) {
                    int catId = t.getCategoryId();
                    catExpenses.put(catId, catExpenses.getOrDefault(catId, 0.0) + t.getAmount());
                } else if ("INCOME".equals(t.getType())) {
                    int catId = t.getCategoryId();
                    catIncome.put(catId, catIncome.getOrDefault(catId, 0.0) + t.getAmount());
                }
            }
            categoryExpenses.setValue(catExpenses);
            categoryExpenses.setValue(catExpenses);
            categoryIncome.setValue(catIncome);

            // Calculate Trends
            calculateTrends(transactions);
        }
    }

    private void calculateTrends(List<Transaction> transactions) {
        // Simple trend analysis: Group by Date (or Month if period is All)
        // For simplicity, let's group by Date string for now.
        // If Period is All, maybe group by Month?
        // Let's stick to grouping by formatted date for provided transactions.

        Map<String, Double> trendMap = new java.util.TreeMap<>(); // Sorted
        java.text.SimpleDateFormat sdf;
        if ("All".equals(currentPeriod)) {
            sdf = new java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.getDefault());
        } else {
            sdf = new java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault());
        }

        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                String key = sdf.format(new java.util.Date(t.getDate()));
                trendMap.put(key, trendMap.getOrDefault(key, 0.0) + t.getAmount());
            }
        }

        List<androidx.core.util.Pair<String, Double>> trendList = new java.util.ArrayList<>();
        for (Map.Entry<String, Double> entry : trendMap.entrySet()) {
            trendList.add(new androidx.core.util.Pair<>(entry.getKey(), entry.getValue()));
        }
        expenseTrend.setValue(trendList);
    }

    private long[] calculateDateRange(String period) {
        Calendar calendar = Calendar.getInstance();
        Long anchorDate = selectedDate.getValue();
        if (anchorDate != null) {
            calendar.setTimeInMillis(anchorDate);
        }

        // Force 2026 if requested, or respect anchor month/day
        calendar.set(Calendar.YEAR, 2026);

        long start = 0;
        long end = 0;

        switch (period) {
            case "Daily":
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                start = calendar.getTimeInMillis();

                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                end = calendar.getTimeInMillis();
                break;
            case "Weekly":
                calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                start = calendar.getTimeInMillis();

                calendar.add(Calendar.DAY_OF_WEEK, 6);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                end = calendar.getTimeInMillis();
                break;
            case "Monthly":
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                start = calendar.getTimeInMillis();

                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                end = calendar.getTimeInMillis();
                break;
            case "Custom":
                return new long[] { customStartDate, customEndDate };
            case "All":
            default:
                start = 0;
                calendar.setTimeInMillis(System.currentTimeMillis());
                calendar.set(Calendar.YEAR, 2026);
                end = calendar.getTimeInMillis();
                break;
        }
        return new long[] { start, end };
    }

    public void refresh() {
        loadDashboardData();
    }
}