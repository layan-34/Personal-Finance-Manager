package edu.birzeit.a1220775_1221026_courseproject.repository;

import android.content.Context;
import android.database.Cursor;

import edu.birzeit.a1220775_1221026_courseproject.data.DatabaseHelper;
import edu.birzeit.a1220775_1221026_courseproject.data.Goal;

import java.util.List;

public class GoalRepository {
    private DatabaseHelper databaseHelper;

    public GoalRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public long insertGoal(Goal goal) {
        return databaseHelper.insertGoal(
                goal.getUserEmail(),
                goal.getName(),
                goal.getTargetAmount(),
                goal.getCurrentAmount(),
                goal.getTargetDate(),
                goal.getStatus());
    }

    public void updateGoal(Goal goal) {
        databaseHelper.updateGoal(goal.getId(), goal.getCurrentAmount(), goal.getStatus());
    }

    public void deleteGoal(Goal goal) {
        databaseHelper.deleteGoal(goal.getId());
    }

    public void deleteGoalById(int goalId) {
        databaseHelper.deleteGoal(goalId);
    }

    public List<Goal> getGoalsByUser(String userEmail) {
        Cursor cursor = databaseHelper.getGoalsByUser(userEmail, null);
        return mapCursorToGoalList(cursor);
    }

    public List<Goal> getGoalsByUserAndStatus(String userEmail, String status) {
        Cursor cursor = databaseHelper.getGoalsByUser(userEmail, status);
        return mapCursorToGoalList(cursor);
    }

    public Goal getGoalById(int goalId) {
        Cursor cursor = databaseHelper.getGoalById(goalId);
        if (cursor != null && cursor.moveToFirst()) {
            Goal goal = mapCursorToGoal(cursor);
            cursor.close();
            return goal;
        }
        if (cursor != null)
            cursor.close();
        return null;
    }

    private List<Goal> mapCursorToGoalList(Cursor cursor) {
        List<Goal> list = new java.util.ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapCursorToGoal(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        if (cursor != null)
            cursor.close();
        return list;
    }

    private Goal mapCursorToGoal(Cursor cursor) {
        Goal g = new Goal();
        g.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        g.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
        g.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        g.setTargetAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount")));
        g.setCurrentAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("current_amount")));
        g.setTargetDate(cursor.getLong(cursor.getColumnIndexOrThrow("target_date")));
        g.setStatus(cursor.getString(cursor.getColumnIndexOrThrow("status")));
        return g;
    }
}
