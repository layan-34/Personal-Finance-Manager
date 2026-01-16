package edu.birzeit.a1220775_1221026_courseproject.ui.budgets;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.slider.Slider;
import androidx.appcompat.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Budget;
import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.data.User;
import edu.birzeit.a1220775_1221026_courseproject.databinding.FragmentBudgetsBinding;
import edu.birzeit.a1220775_1221026_courseproject.repository.BudgetRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;
import edu.birzeit.a1220775_1221026_courseproject.ui.BudgetsAdapter;

public class BudgetsFragment extends Fragment implements BudgetsAdapter.OnBudgetActionListener {

    private FragmentBudgetsBinding binding;
    private BudgetsViewModel budgetsViewModel;
    private BudgetsAdapter adapter;
    private BudgetRepository budgetRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        budgetsViewModel = new ViewModelProvider(this).get(BudgetsViewModel.class);
        budgetRepository = new BudgetRepository(requireContext());
        categoryRepository = new CategoryRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        binding = FragmentBudgetsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        RecyclerView recyclerView = binding.rvBudgets;
        adapter = new BudgetsAdapter(null, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // FAB click
        binding.fabAddBudget.setOnClickListener(v -> showAddBudgetDialog());

        // Observe budgets
        budgetsViewModel.getBudgets().observe(getViewLifecycleOwner(), budgets -> {
            if (budgets != null && !budgets.isEmpty()) {
                adapter.updateBudgets(budgets);
                binding.tvNoBudgets.setVisibility(View.GONE);
                binding.rvBudgets.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoBudgets.setVisibility(View.VISIBLE);
                binding.rvBudgets.setVisibility(View.GONE);
            }
        });

        return root;
    }

    private void showAddBudgetDialog() {
        showBudgetDialog(null);
    }

    @Override
    public void onEditBudget(Budget budget) {
        showBudgetDialog(budget);
    }

    @Override
    public void onDeleteBudget(Budget budget) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    budgetsViewModel.deleteBudget(budget);
                    Toast.makeText(getContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showBudgetDialog(Budget budgetToEdit) {
        boolean isEditMode = budgetToEdit != null;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_budget, null);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        TextInputEditText etBudgetLimit = dialogView.findViewById(R.id.etBudgetLimit);
        TextInputEditText etMonth = dialogView.findViewById(R.id.etMonth);
        Slider sliderAlert = dialogView.findViewById(R.id.sliderAlertThreshold);

        final int[] selectedMonth = { 1 };
        final String[] months = { "January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December" };
        etMonth.setText(months[selectedMonth[0] - 1]);
        etMonth.setOnClickListener(v -> {
            new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                    .setTitle("Select Month")
                    .setItems(months, (d, which) -> {
                        selectedMonth[0] = which + 1;
                        etMonth.setText(months[which]);
                    })
                    .show();
        });

        // Load categories for dropdown
        String userEmail = userRepository.getCurrentUser();
        List<Category> categories = categoryRepository.getCategoriesByUser(userEmail);
        List<String> categoryNames = new ArrayList<>();
        final List<Category> categoryList = new ArrayList<>();
        if (categories != null) {
            for (Category cat : categories) {
                if ("EXPENSE".equals(cat.getType())) {
                    categoryNames.add(cat.getName());
                    categoryList.add(cat);
                }
            }
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);
        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                actvCategory.showDropDown();
        });

        // Pre-fill if Edit Mode
        if (isEditMode) {
            // Find category index
            for (int i = 0; i < categoryList.size(); i++) {
                if (categoryList.get(i).getId() == budgetToEdit.getCategoryId()) {
                    actvCategory.setText(categoryList.get(i).getName(), false);
                    break;
                }
            }
            etBudgetLimit.setText(String.valueOf(budgetToEdit.getLimitAmount()));
            if (budgetToEdit.getMonth() >= 1 && budgetToEdit.getMonth() <= 12) {
                selectedMonth[0] = budgetToEdit.getMonth();
                etMonth.setText(months[selectedMonth[0] - 1]);
            }
            sliderAlert.setValue((float) (budgetToEdit.getAlertRatio() * 100f));
            actvCategory.setEnabled(false);
            actvCategory.setEnabled(true);
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEditMode ? "Edit Budget" : "Add Budget")
                .setView(dialogView)
                .setPositiveButton(isEditMode ? "Update" : "Add", (d, which) -> {
                    try {
                        String categoryText = actvCategory.getText().toString().trim();
                        if (categoryText.isEmpty()) {
                            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int categoryIndex = categoryNames.indexOf(categoryText);
                        if (categoryIndex == -1 || categoryIndex >= categoryList.size()) {
                            Toast.makeText(getContext(), "Invalid category", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Category selectedCategory = categoryList.get(categoryIndex);

                        String limitStr = etBudgetLimit.getText().toString().trim();
                        if (limitStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter budget limit", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        double limit = Double.parseDouble(limitStr);
                        if (limit <= 0) {
                            Toast.makeText(getContext(), "Limit must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int month = selectedMonth[0]; // 1-12
                        double alertRatio = sliderAlert.getValue() / 100.0;

                        if (isEditMode) {
                            budgetToEdit.setCategoryId(selectedCategory.getId());
                            budgetToEdit.setMonth(month);
                            budgetToEdit.setLimitAmount(limit);
                            budgetToEdit.setAlertRatio(alertRatio);
                            budgetsViewModel.updateBudget(budgetToEdit);
                            Toast.makeText(getContext(), "Budget updated", Toast.LENGTH_SHORT).show();
                        } else {
                            User user = userRepository.getUserByEmail(userRepository.getCurrentUser());
                            if (user == null) {
                                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Budget budget = new Budget(user.getEmail(), selectedCategory.getId(), month, limit,
                                    alertRatio);
                            budgetsViewModel.addBudget(budget); // Use ViewModel method
                            Toast.makeText(getContext(), "Budget added", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid number format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (budgetsViewModel != null) {
            budgetsViewModel.refresh();
        }
    }
}