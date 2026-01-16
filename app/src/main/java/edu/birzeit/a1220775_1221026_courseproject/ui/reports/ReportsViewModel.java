package edu.birzeit.a1220775_1221026_courseproject.ui.reports;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;
import edu.birzeit.a1220775_1221026_courseproject.repository.TransactionRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class ReportsViewModel extends AndroidViewModel {
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private MutableLiveData<Double> totalIncome = new MutableLiveData<>();
    private MutableLiveData<Double> totalExpense = new MutableLiveData<>();
    private MutableLiveData<Double> netBalance = new MutableLiveData<>();
    private MutableLiveData<String> reportText = new MutableLiveData<>();
    private MutableLiveData<List<CategoryUsage>> topCategories = new MutableLiveData<>();
    private edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository categoryRepository;

    private String currentPeriod = "Daily";
    private MutableLiveData<Long> selectedDate = new MutableLiveData<>();

    public ReportsViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        userRepository = new UserRepository(application);
        categoryRepository = new edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository(application);

        // Initialize selectedDate to Today (Jan 16, 2026)
        Calendar cal = Calendar.getInstance();
        cal.set(2026, Calendar.JANUARY, 16, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        selectedDate.setValue(cal.getTimeInMillis());
    }

    public LiveData<Long> getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(long date) {
        selectedDate.setValue(date);
        generateReport(currentPeriod);
    }

    public String getSelectedPeriod() {
        return currentPeriod;
    }

    public LiveData<String> getReport() {
        return reportText;
    }

    public LiveData<Double> getTotalIncome() {
        return totalIncome;
    }

    public LiveData<Double> getTotalExpense() {
        return totalExpense;
    }

    public LiveData<Double> getNetBalance() {
        return netBalance;
    }

    public void generateReport(String period) {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty()) {
            reportText.setValue("User not logged in.");
            totalIncome.setValue(0.0);
            totalExpense.setValue(0.0);
            netBalance.setValue(0.0);
            return;
        }

        long[] range = calculateDateRange(period);
        long startDate = range[0];
        long endDate = range[1];

        // Fetch Data
        Double incomeVal = transactionRepository.getTotalByUserTypeAndDateRange(userEmail, "INCOME", startDate,
                endDate);
        Double expenseVal = transactionRepository.getTotalByUserTypeAndDateRange(userEmail, "EXPENSE", startDate,
                endDate);
        List<Transaction> transactions = transactionRepository.getTransactionsByUserAndDateRange(userEmail, startDate,
                endDate);

        double income = incomeVal != null ? incomeVal : 0.0;
        double expense = expenseVal != null ? expenseVal : 0.0;
        double balance = income - expense;

        totalIncome.setValue(income);
        totalExpense.setValue(expense);
        netBalance.setValue(balance);

        StringBuilder sb = new StringBuilder();
        sb.append("Financial Report - ").append(period).append("\n\n");
        sb.append("Period: ").append(formatDate(startDate)).append(" to ").append(formatDate(endDate)).append("\n\n");

        sb.append("SUMMARY\n");
        sb.append("----------------------------\n");
        sb.append(String.format(Locale.getDefault(), "Total Income:   $%.2f\n", income));
        sb.append(String.format(Locale.getDefault(), "Total Expenses: $%.2f\n", expense));
        sb.append("----------------------------\n");
        sb.append(String.format(Locale.getDefault(), "Net Balance:    $%.2f\n\n", balance));

        if (transactions != null && !transactions.isEmpty()) {
            sb.append("TRANSACTIONS DETAILS\n");
            sb.append("----------------------------\n");
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
            for (Transaction t : transactions) {
                String date = sdf.format(new Date(t.getDate()));
                String typeSign = "INCOME".equals(t.getType()) ? "+" : "-";
                sb.append(String.format(Locale.getDefault(), "%s | %-15.15s | %s$%.2f\n",
                        date, t.getDescription(), typeSign, t.getAmount()));
            }
        } else {
            sb.append("No transactions found for this period.");
        }

        reportText.setValue(sb.toString());

        // Calculate Top Categories
        java.util.Map<Integer, Double> catMap = new java.util.HashMap<>();
        if (transactions != null) {
            for (Transaction t : transactions) {
                if ("EXPENSE".equals(t.getType())) {
                    catMap.put(t.getCategoryId(), catMap.getOrDefault(t.getCategoryId(), 0.0) + t.getAmount());
                }
            }
        }

        List<CategoryUsage> usageList = new java.util.ArrayList<>();

        List<edu.birzeit.a1220775_1221026_courseproject.data.Category> allCats = categoryRepository
                .getCategoriesByUser(userEmail);
        java.util.Map<Integer, String> catNameMap = new java.util.HashMap<>();
        if (allCats != null) {
            for (edu.birzeit.a1220775_1221026_courseproject.data.Category c : allCats) {
                catNameMap.put(c.getId(), c.getName());
            }
        }

        for (java.util.Map.Entry<Integer, Double> entry : catMap.entrySet()) {
            String name = catNameMap.getOrDefault(entry.getKey(), "Unknown Category");
            usageList.add(new CategoryUsage(name, entry.getValue()));
        }

        // Sort Descending
        java.util.Collections.sort(usageList, (o1, o2) -> Double.compare(o2.total, o1.total));

        // Take top 5
        if (usageList.size() > 5) {
            usageList = usageList.subList(0, 5);
        }
        topCategories.setValue(usageList);
    }

    private String formatDate(long millis) {
        return new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(new Date(millis));
    }

    private long[] calculateDateRange(String period) {
        Calendar calendar = Calendar.getInstance();
        Long anchorDate = selectedDate.getValue();
        if (anchorDate != null) {
            calendar.setTimeInMillis(anchorDate);
        }

        // Force 2026 if requested
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
            case "Yearly":
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                start = calendar.getTimeInMillis();

                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                calendar.set(Calendar.DAY_OF_MONTH, 31);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                end = calendar.getTimeInMillis();
                break;
            default:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                start = calendar.getTimeInMillis();
                end = System.currentTimeMillis();
                break;
        }
        return new long[] { start, end };
    }

    public LiveData<List<CategoryUsage>> getTopCategories() {
        return topCategories;
    }

    public static class CategoryUsage {
        public String name;
        public double total;

        public CategoryUsage(String name, double total) {
            this.name = name;
            this.total = total;
        }
    }
}
