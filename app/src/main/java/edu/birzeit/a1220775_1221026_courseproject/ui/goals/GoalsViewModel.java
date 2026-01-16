package edu.birzeit.a1220775_1221026_courseproject.ui.goals;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.data.Goal;
import edu.birzeit.a1220775_1221026_courseproject.repository.GoalRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class GoalsViewModel extends AndroidViewModel {
    private GoalRepository goalRepository;
    private UserRepository userRepository;
    private MutableLiveData<List<Goal>> goals = new MutableLiveData<>();
    private MutableLiveData<String> filterStatus = new MutableLiveData<>("ALL"); // "ALL", "ACTIVE", "COMPLETED"

    public GoalsViewModel(@NonNull Application application) {
        super(application);
        goalRepository = new GoalRepository(application);
        userRepository = new UserRepository(application);
        loadGoals();
    }

    public LiveData<List<Goal>> getGoals() {
        return goals;
    }

    public LiveData<String> getFilterStatus() {
        return filterStatus;
    }

    public void loadGoals() {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        String status = filterStatus.getValue();
        List<Goal> goalList;
        if (status == null || "ALL".equals(status)) {
            goalList = goalRepository.getGoalsByUser(userEmail);
        } else {
            goalList = goalRepository.getGoalsByUserAndStatus(userEmail, status);
        }
        goals.setValue(goalList);
    }

    public void setFilterStatus(String status) {
        filterStatus.setValue(status);
        loadGoals();
    }

    public void addGoal(Goal goal) {
        goalRepository.insertGoal(goal);
        loadGoals();
    }

    public void updateGoal(Goal goal) {
        goalRepository.updateGoal(goal);
        loadGoals();
    }

    public void deleteGoal(Goal goal) {
        goalRepository.deleteGoal(goal);
        loadGoals();
    }

    public void refresh() {
        loadGoals();
    }
}
