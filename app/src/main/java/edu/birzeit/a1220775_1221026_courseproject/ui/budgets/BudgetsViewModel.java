package edu.birzeit.a1220775_1221026_courseproject.ui.budgets;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.data.Budget;
import edu.birzeit.a1220775_1221026_courseproject.repository.BudgetRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class BudgetsViewModel extends AndroidViewModel {
    private BudgetRepository budgetRepository;
    private UserRepository userRepository;
    private MutableLiveData<List<edu.birzeit.a1220775_1221026_courseproject.data.BudgetWithSpent>> budgets = new MutableLiveData<>();

    public BudgetsViewModel(@NonNull Application application) {
        super(application);
        budgetRepository = new BudgetRepository(application);
        userRepository = new UserRepository(application);
        loadBudgets();
    }

    public LiveData<List<edu.birzeit.a1220775_1221026_courseproject.data.BudgetWithSpent>> getBudgets() {
        return budgets;
    }

    public void loadBudgets() {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        List<edu.birzeit.a1220775_1221026_courseproject.data.BudgetWithSpent> budgetList = budgetRepository
                .getBudgetsWithSpent(userEmail);
        budgets.setValue(budgetList);
    }

    public void addBudget(Budget budget) {
        budgetRepository.insertBudget(budget);
        loadBudgets();
    }

    public void updateBudget(Budget budget) {
        budgetRepository.updateBudget(budget);
        loadBudgets();
    }

    public void deleteBudget(Budget budget) {
        budgetRepository.deleteBudget(budget);
        loadBudgets();
    }

    public void refresh() {
        loadBudgets();
    }
}
