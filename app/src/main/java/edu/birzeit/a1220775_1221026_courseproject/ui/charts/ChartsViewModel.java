package edu.birzeit.a1220775_1221026_courseproject.ui.charts;

import android.app.Application;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;
import edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.TransactionRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class ChartsViewModel extends AndroidViewModel {
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private CategoryRepository categoryRepository;

    private MutableLiveData<PieChartData> categoryPieData = new MutableLiveData<>();
    private MutableLiveData<PieChartData> incomeExpensePieData = new MutableLiveData<>();
    private MutableLiveData<List<Pair<String, Double>>> expenseTrend = new MutableLiveData<>();

    public ChartsViewModel(@NonNull Application application) {
        super(application);
        transactionRepository = new TransactionRepository(application);
        userRepository = new UserRepository(application);
        categoryRepository = new CategoryRepository(application);
        loadChartData();
    }

    public LiveData<PieChartData> getCategoryPieData() {
        return categoryPieData;
    }

    public LiveData<PieChartData> getIncomeExpensePieData() {
        return incomeExpensePieData;
    }

    public LiveData<List<Pair<String, Double>>> getExpenseTrend() {
        return expenseTrend;
    }

    public void loadChartData() {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty())
            return;

        List<Transaction> transactions = transactionRepository.getTransactionsByUser(userEmail);

        if (transactions != null) {
            processCategoryExpenses(transactions, userEmail);
            processIncomeVsExpense(transactions);
            processTrend(transactions);
        }
    }

    private void processCategoryExpenses(List<Transaction> transactions, String userEmail) {
        // Fetch categories to map ID to Name
        List<edu.birzeit.a1220775_1221026_courseproject.data.Category> cats = categoryRepository
                .getCategoriesByUser(userEmail);
        Map<Integer, String> catNames = new HashMap<>();
        if (cats != null) {
            for (edu.birzeit.a1220775_1221026_courseproject.data.Category c : cats) {
                catNames.put(c.getId(), c.getName());
            }
        }

        Map<String, Double> catMap = new HashMap<>();
        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                String name = catNames.getOrDefault(t.getCategoryId(), "Unknown");
                catMap.put(name, catMap.getOrDefault(name, 0.0) + t.getAmount());
            }
        }

        List<Float> values = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        // Beautiful Colors
        int[] materialColors = {
                Color.parseColor("#EF5350"), Color.parseColor("#EC407A"), Color.parseColor("#AB47BC"),
                Color.parseColor("#7E57C2"), Color.parseColor("#5C6BC0"), Color.parseColor("#42A5F5"),
                Color.parseColor("#29B6F6"), Color.parseColor("#26C6DA"), Color.parseColor("#26A69A"),
                Color.parseColor("#66BB6A"), Color.parseColor("#9CCC65"), Color.parseColor("#D4E157")
        };

        int i = 0;
        for (Map.Entry<String, Double> entry : catMap.entrySet()) {
            values.add(entry.getValue().floatValue());
            labels.add(entry.getKey());
            colors.add(materialColors[i % materialColors.length]);
            i++;
        }

        categoryPieData.setValue(new PieChartData(values, colors, labels));
    }

    private void processIncomeVsExpense(List<Transaction> transactions) {
        double income = 0;
        double expense = 0;
        for (Transaction t : transactions) {
            if ("INCOME".equals(t.getType()))
                income += t.getAmount();
            else if ("EXPENSE".equals(t.getType()))
                expense += t.getAmount();
        }

        List<Float> values = new ArrayList<>();
        values.add((float) income);
        values.add((float) expense);

        List<String> labels = new ArrayList<>();
        labels.add("Income");
        labels.add("Expense");

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#66BB6A")); // Green
        colors.add(Color.parseColor("#EF5350")); // Red

        incomeExpensePieData.setValue(new PieChartData(values, colors, labels));
    }

    private void processTrend(List<Transaction> transactions) {
        SimpleDateFormat sdfKey = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        SimpleDateFormat sdfDisplay = new SimpleDateFormat("MMM", Locale.getDefault());

        Map<String, Double> chronologicalMap = new TreeMap<>();

        for (Transaction t : transactions) {
            if ("EXPENSE".equals(t.getType())) {
                String key = sdfKey.format(new Date(t.getDate()));
                chronologicalMap.put(key, chronologicalMap.getOrDefault(key, 0.0) + t.getAmount());
            }
        }

        List<Pair<String, Double>> trendList = new ArrayList<>();
        // Last 6 months
        int count = 0;
        int start = Math.max(0, chronologicalMap.size() - 6);

        for (Map.Entry<String, Double> entry : chronologicalMap.entrySet()) {
            if (count >= start) {
                try {
                    Date d = sdfKey.parse(entry.getKey());
                    String label = sdfDisplay.format(d);
                    trendList.add(new Pair<>(label, entry.getValue()));
                } catch (Exception e) {
                    trendList.add(new Pair<>(entry.getKey(), entry.getValue()));
                }
            }
            count++;
        }
        expenseTrend.setValue(trendList);
    }

    public static class PieChartData {
        public List<Float> values;
        public List<Integer> colors;
        public List<String> labels;

        public PieChartData(List<Float> v, List<Integer> c, List<String> l) {
            values = v;
            colors = c;
            labels = l;
        }
    }
}
