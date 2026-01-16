package edu.birzeit.a1220775_1221026_courseproject.ui.gallery;

import androidx.appcompat.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;
import edu.birzeit.a1220775_1221026_courseproject.data.User;
import edu.birzeit.a1220775_1221026_courseproject.databinding.FragmentGalleryBinding;
import edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.TransactionRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;
import edu.birzeit.a1220775_1221026_courseproject.ui.transactions.TransactionsViewModel;
import edu.birzeit.a1220775_1221026_courseproject.ui.TransactionsAdapter;

public class GalleryFragment extends Fragment {

    private FragmentGalleryBinding binding;
    private TransactionsViewModel transactionsViewModel;
    private TransactionsAdapter adapter;
    private TransactionRepository transactionRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;
    private long selectedDate = System.currentTimeMillis();
    private Map<Integer, String> categoryCache = new HashMap<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        transactionsViewModel = new ViewModelProvider(this).get(TransactionsViewModel.class);
        transactionRepository = new TransactionRepository(requireContext());
        categoryRepository = new CategoryRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        binding = FragmentGalleryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Check for args
        if (getArguments() != null) {
            String filterType = getArguments().getString("filter_type");
            if (filterType != null) {
                transactionsViewModel.setFilterType(filterType);
                // Hide filter buttons if a specific type is forced
                binding.btnFilterAll.setVisibility(View.GONE);
                binding.btnFilterIncome.setVisibility(View.GONE);
                binding.btnFilterExpense.setVisibility(View.GONE);

                // Show Add Button for specific types
                binding.fabAddTransaction.setVisibility(View.VISIBLE);
                // Hide Sort for specific types (optional, keeping it simple as user asked for
                // it on 'all trans window')
                binding.spinnerSort.setVisibility(View.GONE);
            }
        }

        // Initialize RecyclerView
        RecyclerView recyclerView = binding.rvTransactions;
        adapter = new TransactionsAdapter(null, new TransactionsAdapter.OnTransactionClickListener() {
            @Override
            public void onEditClick(Transaction transaction) {
                showEditTransactionDialog(transaction);
            }

            @Override
            public void onDeleteClick(Transaction transaction) {
                showDeleteConfirmationDialog(transaction);
            }
        });

        // Load categories for adapter cache
        String userEmail = userRepository.getCurrentUser();
        List<Category> categories = categoryRepository.getCategoriesByUser(userEmail);
        categoryCache.clear();
        if (categories != null) {
            for (Category cat : categories) {
                categoryCache.put(cat.getId(), cat.getName());
            }
        }
        adapter.setCategoryCache(categoryCache);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Filter buttons
        binding.btnFilterAll.setOnClickListener(v -> {
            transactionsViewModel.setFilterType("ALL");
            binding.fabAddTransaction.setVisibility(View.GONE);
            binding.spinnerSort.setVisibility(View.VISIBLE);
        });
        binding.btnFilterIncome.setOnClickListener(v -> {
            transactionsViewModel.setFilterType("INCOME");
            binding.fabAddTransaction.setVisibility(View.VISIBLE);
            binding.spinnerSort.setVisibility(View.GONE);
        });
        binding.btnFilterExpense.setOnClickListener(v -> {
            transactionsViewModel.setFilterType("EXPENSE");
            binding.fabAddTransaction.setVisibility(View.VISIBLE);
            binding.spinnerSort.setVisibility(View.GONE);
        });

        // FAB click
        binding.fabAddTransaction.setOnClickListener(v -> showAddTransactionDialog());

        // Sort Spinner
        android.widget.Spinner spinnerSort = binding.spinnerSort;
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[] { "Date: Newest First", "Date: Oldest First" });
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(sortAdapter);

        spinnerSort.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    transactionsViewModel.setSortOrder("DESC");
                } else {
                    transactionsViewModel.setSortOrder("ASC");
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {
            }
        });

        // Default visibility (ALL Transactions) - only set if no filter_type argument
        if (getArguments() == null || getArguments().getString("filter_type") == null) {
            binding.fabAddTransaction.setVisibility(View.GONE); // Hide Add by default for ALL
            binding.spinnerSort.setVisibility(View.VISIBLE); // Show Sort by default for ALL
        }

        // Observe transactions
        transactionsViewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null && !transactions.isEmpty()) {
                adapter.updateTransactions(transactions);
                binding.tvNoTransactions.setVisibility(View.GONE);
                binding.rvTransactions.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoTransactions.setVisibility(View.VISIBLE);
                binding.rvTransactions.setVisibility(View.GONE);
            }
        });

        return root;
    }

    private void showEditTransactionDialog(Transaction transaction) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etAmount);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        com.google.android.material.button.MaterialButtonToggleGroup toggle = dialogView
                .findViewById(R.id.toggleTransactionType);
        android.widget.Button btnIncome = dialogView.findViewById(R.id.btnIncome);
        android.widget.Button btnExpense = dialogView.findViewById(R.id.btnExpense);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);

        // Pre-fill data
        etAmount.setText(String.valueOf(transaction.getAmount()));
        etDescription.setText(transaction.getDescription());
        if ("INCOME".equals(transaction.getType())) {
            toggle.check(btnIncome.getId());
        } else {
            toggle.check(btnExpense.getId());
        }



        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(transaction.getDate());
        selectedDate = transaction.getDate();
        etDate.setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(selectedDate)));

        // Setup Date Picker
        View.OnClickListener dateClickListener = v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, dayOfMonth);
                        selectedDate = cal.getTimeInMillis();
                        etDate
                                .setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                        .format(new java.util.Date(selectedDate)));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        };
        etDate.setOnClickListener(dateClickListener);

        // Define lists for scope access
        List<String> categoryNames = new ArrayList<>();
        final List<Category> categoryList = new ArrayList<>();


        Runnable loadCategories = () -> {
            String type = (toggle.getCheckedButtonId() == btnIncome.getId()) ? "INCOME" : "EXPENSE";
            String email = userRepository.getCurrentUser();
            List<Category> filteredCategories = categoryRepository.getCategoriesByUserAndType(email, type);

            categoryNames.clear();
            categoryList.clear();

            if (filteredCategories != null) {
                for (Category cat : filteredCategories) {
                    categoryNames.add(cat.getName());
                    categoryList.add(cat);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, categoryNames);
            actvCategory.setAdapter(adapter);

            // Check if current category is still valid, else clear
            if (!categoryNames.contains(actvCategory.getText().toString())) {
                if (!categoryNames.isEmpty()) {
                    // Optional: auto-select first or just clear
                    actvCategory.setText("", false);
                } else {
                    actvCategory.setText("", false);
                }
            }
        };


        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                loadCategories.run();
            }
        });

        // Trigger initial load based on pre-filled type
        loadCategories.run();

        // Restore category selection after adapter reload
        String currentName = transaction.getCategoryId() != 0 ? categoryCache.get(transaction.getCategoryId()) : "";
        if (currentName != null && categoryNames.contains(currentName)) {
            actvCategory.setText(currentName, false);
        } else if (transaction.getCategoryId() != 0) {

        }

        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                actvCategory.showDropDown();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Transaction")
                .setView(dialogView)
                .setPositiveButton("Update", (d, which) -> {
                    try {
                        String amountStr = etAmount.getText().toString().trim();
                        if (amountStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

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

                        String type = (toggle.getCheckedButtonId() == btnIncome.getId()) ? "INCOME" : "EXPENSE";
                        String description = etDescription.getText().toString().trim();

                        transaction.setAmount(amount);
                        transaction.setType(type);
                        transaction.setCategoryId(selectedCategory.getId());
                        transaction.setDescription(description);
                        transaction.setDate(selectedDate);

                        transactionsViewModel.updateTransaction(transaction);
                        Toast.makeText(getContext(), "Transaction updated", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showDeleteConfirmationDialog(Transaction transaction) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    transactionsViewModel.deleteTransaction(transaction);
                    Toast.makeText(getContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddTransactionDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_transaction, null);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etAmount);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);
        com.google.android.material.button.MaterialButtonToggleGroup toggle = dialogView
                .findViewById(R.id.toggleTransactionType);
        android.widget.Button btnIncome = dialogView.findViewById(R.id.btnIncome);
        android.widget.Button btnExpense = dialogView.findViewById(R.id.btnExpense);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);

        // Setup Dynamic Category Loader
        List<String> categoryNames = new ArrayList<>();
        final List<Category> categoryList = new ArrayList<>();

        Runnable loadCategories = () -> {
            String type = (toggle.getCheckedButtonId() == btnIncome.getId()) ? "INCOME" : "EXPENSE";
            String email = userRepository.getCurrentUser();
            List<Category> filteredCategories = categoryRepository.getCategoriesByUserAndType(email, type);

            categoryNames.clear();
            categoryList.clear();

            if (filteredCategories != null) {
                for (Category cat : filteredCategories) {
                    categoryNames.add(cat.getName());
                    categoryList.add(cat);
                }
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, categoryNames);
            actvCategory.setAdapter(adapter);
            actvCategory.setText("", false); // Clear selection on type change
        };

        // Toggle selection listener
        toggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                loadCategories.run();
            }
        });

        // Handle filter type argument to set default RadioButton
        if (getArguments() != null) {
            String filterType = getArguments().getString("filter_type");
            if ("INCOME".equals(filterType)) {
                toggle.check(btnIncome.getId());
            } else if ("EXPENSE".equals(filterType)) {
                toggle.check(btnExpense.getId());
            }
        }

        // Initial load
        loadCategories.run();

        actvCategory.setOnClickListener(v -> actvCategory.showDropDown());
        actvCategory.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus)
                actvCategory.showDropDown();
        });

        // Date picker
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2026);
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        selectedDate = calendar.getTimeInMillis();
        etDate.setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date(selectedDate)));
        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        Calendar cal = Calendar.getInstance();
                        cal.set(year, month, dayOfMonth);
                        selectedDate = cal.getTimeInMillis();
                        etDate
                                .setText(new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                                        .format(new java.util.Date(selectedDate)));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Transaction")
                .setView(dialogView)
                .setPositiveButton("Add", (d, which) -> {
                    try {
                        String amountStr = etAmount.getText().toString().trim();
                        if (amountStr.isEmpty()) {
                            Toast.makeText(getContext(), "Please enter amount", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            Toast.makeText(getContext(), "Amount must be greater than 0", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String categoryText = actvCategory.getText().toString().trim();
                        if (categoryText.isEmpty()) {
                            Toast.makeText(getContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Find selected category
                        int categoryIndex = categoryNames.indexOf(categoryText);
                        if (categoryIndex == -1 || categoryIndex >= categoryList.size()) {
                            Toast.makeText(getContext(), "Invalid category", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Category selectedCategory = categoryList.get(categoryIndex);

                        String type = (toggle.getCheckedButtonId() == btnIncome.getId()) ? "INCOME" : "EXPENSE";
                        String description = etDescription.getText().toString().trim();

                        String userEmail = userRepository.getCurrentUser();
                        User user = userRepository.getUserByEmail(userEmail);
                        if (user == null) {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        Transaction transaction = new Transaction(user.getEmail(), type, amount, selectedDate,
                                selectedCategory.getId(), description);
                        transactionRepository.insertTransaction(transaction);
                        transactionsViewModel.refresh();
                        Toast.makeText(getContext(), "Transaction added", Toast.LENGTH_SHORT).show();
                    } catch (NumberFormatException e) {
                        Toast.makeText(getContext(), "Invalid amount format", Toast.LENGTH_SHORT).show();
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
        if (transactionsViewModel != null) {
            transactionsViewModel.refresh();
        }
    }
}