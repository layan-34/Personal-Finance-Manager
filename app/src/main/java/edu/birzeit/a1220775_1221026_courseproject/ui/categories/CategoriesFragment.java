package edu.birzeit.a1220775_1221026_courseproject.ui.categories;

import androidx.appcompat.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.databinding.FragmentCategoriesBinding;
import edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;
import edu.birzeit.a1220775_1221026_courseproject.ui.CategoriesAdapter;

public class CategoriesFragment extends Fragment {

    private FragmentCategoriesBinding binding;
    private CategoriesViewModel categoriesViewModel;
    private CategoriesAdapter adapter;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        categoriesViewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        categoryRepository = new CategoryRepository(requireContext());
        userRepository = new UserRepository(requireContext());

        binding = FragmentCategoriesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize RecyclerView
        RecyclerView recyclerView = binding.rvCategories;
        adapter = new CategoriesAdapter(null, new CategoriesAdapter.OnCategoryClickListener() {
            @Override
            public void onEditClick(Category category) {
                showEditCategoryDialog(category);
            }

            @Override
            public void onDeleteClick(Category category) {
                showDeleteConfirmationDialog(category);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Filter via TabLayout (All, Income, Expense)
        TabLayout tabLayout = binding.tabLayout;
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int pos = tab.getPosition();
                if (pos == 0) {
                    categoriesViewModel.setFilterType("ALL");
                } else if (pos == 1) {
                    categoriesViewModel.setFilterType("INCOME");
                } else {
                    categoriesViewModel.setFilterType("EXPENSE");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        // FAB click
        binding.fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        // Observe categories
        categoriesViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                adapter.updateCategories(categories);
                binding.tvNoCategories.setVisibility(View.GONE);
                binding.rvCategories.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoCategories.setVisibility(View.VISIBLE);
                binding.rvCategories.setVisibility(View.GONE);
            }
        });

        return root;
    }

    private void showEditCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etCategoryName);
        MaterialButtonToggleGroup toggle = dialogView.findViewById(R.id.toggleCategoryType);
        Button btnIncome = dialogView.findViewById(R.id.btnTypeIncome);
        Button btnExpense = dialogView.findViewById(R.id.btnTypeExpense);

        etName.setText(category.getName());
        if ("INCOME".equals(category.getType())) {
            toggle.check(btnIncome.getId());
        } else {
            toggle.check(btnExpense.getId());
        }

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Edit Category")
                .setView(dialogView)
                .setPositiveButton("Update", (d, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter category name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int checkedId = toggle.getCheckedButtonId();
                    String type = (checkedId == btnIncome.getId()) ? "INCOME" : "EXPENSE";

                    category.setName(name);
                    category.setType(type);
                    categoriesViewModel.updateCategory(category);
                    Toast.makeText(getContext(), "Category updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    private void showDeleteConfirmationDialog(Category category) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete " + category.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    categoriesViewModel.deleteCategory(category);
                    Toast.makeText(getContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCategoryDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_category, null);
        TextInputEditText etName = dialogView.findViewById(R.id.etCategoryName);
        MaterialButtonToggleGroup toggle = dialogView.findViewById(R.id.toggleCategoryType);
        Button btnIncome = dialogView.findViewById(R.id.btnTypeIncome);
        Button btnExpense = dialogView.findViewById(R.id.btnTypeExpense);

        AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setTitle("Add Category")
                .setView(dialogView)
                .setPositiveButton("Add", (d, which) -> {
                    String name = etName.getText().toString().trim();
                    if (name.isEmpty()) {
                        Toast.makeText(getContext(), "Please enter category name", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int checkedId = toggle.getCheckedButtonId();
                    String type = (checkedId == btnIncome.getId()) ? "INCOME" : "EXPENSE";
                    String userEmail = userRepository.getCurrentUser();
                    if (userEmail == null || userEmail.isEmpty()) {
                        Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    edu.birzeit.a1220775_1221026_courseproject.data.User user = userRepository
                            .getUserByEmail(userEmail);
                    if (user == null) {
                        Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Category newCategory = new Category(user.getEmail(), type, name, null, null);
                    categoriesViewModel.addCategory(newCategory);
                    Toast.makeText(getContext(), "Category added", Toast.LENGTH_SHORT).show();
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
        if (categoriesViewModel != null) {
            categoriesViewModel.refresh();
        }
    }
}