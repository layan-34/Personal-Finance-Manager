package edu.birzeit.a1220775_1221026_courseproject.ui.goals;

import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Goal;
import edu.birzeit.a1220775_1221026_courseproject.data.User;
import edu.birzeit.a1220775_1221026_courseproject.databinding.FragmentGoalsBinding;
import edu.birzeit.a1220775_1221026_courseproject.repository.GoalRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;
import edu.birzeit.a1220775_1221026_courseproject.ui.GoalsAdapter;

public class GoalsFragment extends Fragment implements GoalsAdapter.OnGoalActionListener {

    private FragmentGoalsBinding binding;
    private GoalsViewModel goalsViewModel;
    private GoalsAdapter adapter;
    private GoalRepository goalRepository;
    private UserRepository userRepository;
    private Long selectedTargetDate = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        goalsViewModel = new ViewModelProvider(this).get(GoalsViewModel.class);
        goalRepository = new GoalRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        binding = FragmentGoalsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        RecyclerView recyclerView = binding.rvGoals;
        adapter = new GoalsAdapter(null, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Filter buttons
        binding.btnFilterAll.setOnClickListener(v -> goalsViewModel.setFilterStatus("ALL"));
        binding.btnFilterActive.setOnClickListener(v -> goalsViewModel.setFilterStatus("ACTIVE"));
        binding.btnFilterCompleted.setOnClickListener(v -> goalsViewModel.setFilterStatus("COMPLETED"));

        // FAB click
        binding.fabAddGoal.setOnClickListener(v -> showGoalDialog(null));

        // Observe goals
        goalsViewModel.getGoals().observe(getViewLifecycleOwner(), goals -> {
            if (goals != null && !goals.isEmpty()) {
                adapter.updateGoals(goals);
                binding.tvNoGoals.setVisibility(View.GONE);
                binding.rvGoals.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoGoals.setVisibility(View.VISIBLE);
                binding.rvGoals.setVisibility(View.GONE);
            }
        });

        return root;
    }

    @Override
    public void onAddFunds(Goal goal) {
        showAddFundsDialog(goal);
    }

    @Override
    public void onEditGoal(Goal goal) {
        showGoalDialog(goal);
    }

    @Override
    public void onDeleteGoal(Goal goal) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Goal")
                .setMessage("Are you sure you want to delete this goal?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    goalRepository.deleteGoal(goal);
                    goalsViewModel.refresh();
                    Toast.makeText(getContext(), "Goal deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddFundsDialog(Goal goal) {
        android.widget.EditText input = new android.widget.EditText(getContext());
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint("Amount to add");

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Funds to " + goal.getName())
                .setMessage("Current: " + goal.getCurrentAmount() + "\nTarget: " + goal.getTargetAmount())
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String text = input.getText().toString();
                    if (!text.isEmpty()) {
                        try {
                            double added = Double.parseDouble(text);
                            if (added > 0) {
                                goal.setCurrentAmount(goal.getCurrentAmount() + added);
                                if (goal.getCurrentAmount() >= goal.getTargetAmount()) {
                                    goal.setStatus("COMPLETED");
                                    Toast.makeText(getContext(), "Goal Completed!", Toast.LENGTH_LONG).show();
                                }
                                goalsViewModel.updateGoal(goal);
                            } else {
                                Toast.makeText(getContext(), "Amount must be positive", Toast.LENGTH_SHORT).show();
                            }
                        } catch (NumberFormatException e) {
                            Toast.makeText(getContext(), "Invalid amount", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showGoalDialog(Goal goalToEdit) {
        boolean isEditMode = goalToEdit != null;
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_goal, null);
        TextInputEditText etGoalName = dialogView.findViewById(R.id.etGoalName);
        TextInputEditText etTargetAmount = dialogView.findViewById(R.id.etTargetAmount);
        TextInputEditText etCurrentAmount = dialogView.findViewById(R.id.etCurrentAmount);
        TextInputEditText etTargetDate = dialogView.findViewById(R.id.etTargetDate);

        selectedTargetDate = null;
        if (isEditMode) {
            etGoalName.setText(goalToEdit.getName());
            etTargetAmount.setText(String.valueOf(goalToEdit.getTargetAmount()));
            etCurrentAmount.setText(String.valueOf(goalToEdit.getCurrentAmount()));
            selectedTargetDate = goalToEdit.getTargetDate();
            if (selectedTargetDate != null) {
                etTargetDate
                        .setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                .format(new java.util.Date(selectedTargetDate)));
            }
        }

        if (etTargetDate.getText() == null || etTargetDate.getText().toString().isEmpty()) {
            etTargetDate.setHint("Select Target Date (Optional)");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2026);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        if (selectedTargetDate != null) {
            calendar.setTimeInMillis(selectedTargetDate);
        }

        etTargetDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, dayOfMonth);
                        selectedTargetDate = cal.getTimeInMillis();
                        etTargetDate
                                .setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                        .format(new java.util.Date(selectedTargetDate)));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(isEditMode ? "Edit Goal" : "Add Goal")
                .setView(dialogView)
                .setPositiveButton(isEditMode ? "Update" : "Add", (d, which) -> {
                    try {
                        String name = etGoalName.getText().toString().trim();
                        if (name.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter goal name", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String targetStr = etTargetAmount.getText().toString().trim();
                        if (targetStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter target amount", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        double targetAmount = Double.parseDouble(targetStr);
                        if (targetAmount <= 0) {
                            Toast.makeText(getContext(), "Target amount must be greater than 0", Toast.LENGTH_SHORT)
                                    .show();
                            return;
                        }

                        double currentAmount = 0.0;
                        String currentStr = etCurrentAmount.getText().toString().trim();
                        if (!currentStr.isEmpty()) {
                            currentAmount = Double.parseDouble(currentStr);
                            if (currentAmount < 0) {
                                Toast.makeText(getContext(), "Current amount cannot be negative", Toast.LENGTH_SHORT)
                                        .show();
                                return;
                            }
                        }

                        String status = "ACTIVE";
                        if (currentAmount >= targetAmount) {
                            status = "COMPLETED";
                        }

                        if (isEditMode) {
                            goalToEdit.setName(name);
                            goalToEdit.setTargetAmount(targetAmount);
                            goalToEdit.setCurrentAmount(currentAmount);
                            goalToEdit.setTargetDate(selectedTargetDate);
                            goalToEdit.setStatus(status);
                            goalsViewModel.updateGoal(goalToEdit);
                            Toast.makeText(getContext(), "Goal updated", Toast.LENGTH_SHORT).show();
                        } else {
                            String userEmail = userRepository.getCurrentUser();
                            User user = userRepository.getUserByEmail(userEmail);
                            if (user == null) {
                                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Goal goal = new Goal(user.getEmail(), name, targetAmount, currentAmount, selectedTargetDate,
                                    status);
                            goalRepository.insertGoal(goal);
                            goalsViewModel.refresh();
                            Toast.makeText(getContext(), "Goal added", Toast.LENGTH_SHORT).show();
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
        if (goalsViewModel != null) {
            goalsViewModel.refresh();
        }
    }
}