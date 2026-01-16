package edu.birzeit.a1220775_1221026_courseproject.repository;

import android.content.Context;
import android.database.Cursor;

import edu.birzeit.a1220775_1221026_courseproject.data.Budget;
import edu.birzeit.a1220775_1221026_courseproject.data.BudgetWithSpent;
import edu.birzeit.a1220775_1221026_courseproject.data.DatabaseHelper;

import java.util.List;

public class BudgetRepository {
    private DatabaseHelper databaseHelper;

    public BudgetRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public long insertBudget(Budget budget) {
        return databaseHelper.insertBudget(
                budget.getUserEmail(),
                budget.getCategoryId(),
                budget.getMonth(),
                budget.getLimitAmount(),
                budget.getAlertRatio());
    }

    public void updateBudget(Budget budget) {
        databaseHelper.updateBudget(budget.getId(), budget.getLimitAmount(), budget.getAlertRatio());
    }

    public void deleteBudget(Budget budget) {
        databaseHelper.deleteBudget(budget.getId());
    }

    public void deleteBudgetById(int budgetId) {
        databaseHelper.deleteBudget(budgetId);
    }

    public List<Budget> getBudgetsByUser(String userEmail) {
        Cursor cursor = databaseHelper.getBudgetsByUser(userEmail, null);
        return mapCursorToBudgetList(cursor);
    }

    public List<Budget> getBudgetsByUserAndMonth(String userEmail, int month) {
        Cursor cursor = databaseHelper.getBudgetsByUser(userEmail, month);
        return mapCursorToBudgetList(cursor);
    }

    public Budget getBudgetById(int budgetId) {
        Cursor cursor = databaseHelper.getBudgetById(budgetId);
        if (cursor != null && cursor.moveToFirst()) {
            Budget budget = mapCursorToBudget(cursor);
            cursor.close();
            return budget;
        }
        if (cursor != null)
            cursor.close();
        return null;
    }

    public List<BudgetWithSpent> getBudgetsWithSpent(String userEmail) {
        List<Budget> userBudgets = getBudgetsByUser(userEmail);
        List<BudgetWithSpent> result = new java.util.ArrayList<>();

        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int currentYear = 2026;

        for (Budget b : userBudgets) {
            int monthIndex = b.getMonth() - 1;
            calendar.set(currentYear, monthIndex, 1, 0, 0, 0);
            calendar.set(java.util.Calendar.MILLISECOND, 0);
            long startDate = calendar.getTimeInMillis();

            calendar.set(java.util.Calendar.DAY_OF_MONTH, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH));
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23);
            calendar.set(java.util.Calendar.MINUTE, 59);
            calendar.set(java.util.Calendar.SECOND, 59);
            calendar.set(java.util.Calendar.MILLISECOND, 999);
            long endDate = calendar.getTimeInMillis();

            Double spent = databaseHelper.getSpentByCategoryAndDateRange(userEmail, b.getCategoryId(), startDate,
                    endDate);

            Cursor catCursor = databaseHelper.getCategoryById(b.getCategoryId());
            String categoryName = "Unknown";
            if (catCursor != null && catCursor.moveToFirst()) {
                categoryName = catCursor.getString(catCursor.getColumnIndexOrThrow("name"));
                catCursor.close();
            } else if (catCursor != null) {
                catCursor.close();
            }

            result.add(new BudgetWithSpent(b, spent != null ? spent : 0.0, categoryName));
        }
        return result;
    }

    private List<Budget> mapCursorToBudgetList(Cursor cursor) {
        List<Budget> list = new java.util.ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapCursorToBudget(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        if (cursor != null)
            cursor.close();
        return list;
    }

    private Budget mapCursorToBudget(Cursor cursor) {
        Budget b = new Budget();
        b.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        b.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
        b.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
        b.setMonth(cursor.getInt(cursor.getColumnIndexOrThrow("month")));
        b.setLimitAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("limit_amount")));
        b.setAlertRatio(cursor.getDouble(cursor.getColumnIndexOrThrow("alert_ratio")));
        return b;
    }
}
