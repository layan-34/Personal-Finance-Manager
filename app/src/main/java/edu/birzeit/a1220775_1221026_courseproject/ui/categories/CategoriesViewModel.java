package edu.birzeit.a1220775_1221026_courseproject.ui.categories;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.repository.CategoryRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.TransactionRepository;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class CategoriesViewModel extends AndroidViewModel {
    private CategoryRepository categoryRepository;
    private TransactionRepository transactionRepository;
    private UserRepository userRepository;
    private MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private MutableLiveData<String> filterType = new MutableLiveData<>("ALL"); // "ALL", "INCOME", "EXPENSE"

    public CategoriesViewModel(@NonNull Application application) {
        super(application);
        categoryRepository = new CategoryRepository(application);
        transactionRepository = new TransactionRepository(application);
        userRepository = new UserRepository(application);
        loadCategories();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<String> getFilterType() {
        return filterType;
    }

    public void loadCategories() {
        String userEmail = userRepository.getCurrentUser();
        if (userEmail == null || userEmail.isEmpty()) {
            return;
        }

        String type = filterType.getValue();
        List<Category> categoryList;
        if (type == null || "ALL".equals(type)) {
            categoryList = categoryRepository.getCategoriesByUser(userEmail);
        } else {
            categoryList = categoryRepository.getCategoriesByUserAndType(userEmail, type);
        }
        categories.setValue(categoryList);
    }

    public void setFilterType(String type) {
        filterType.setValue(type);
        loadCategories();
    }

    public void addCategory(Category category) {
        categoryRepository.insertCategory(category);
        loadCategories();
    }

    public void updateCategory(Category category) {
        categoryRepository.updateCategory(category);
        loadCategories();
    }

    public void deleteCategory(Category category) {
        // 1. Check if transactions exist for this category
        int transactionCount = transactionRepository.countTransactionsByCategory(category.getId());

        if (transactionCount > 0) {
            // 2. Find or create "Others" category
            Category othersCategory = categoryRepository.getCategoryByNameAndType(
                    category.getUserEmail(), "Others", category.getType());

            int newCategoryId;
            if (othersCategory == null) {
                // Create "Others" category if it doesn't exist
                othersCategory = new Category(category.getUserEmail(), category.getType(), "Others", "#9E9E9E",
                        "ic_menu_camera");
                // Note: Using a default color (Grey) and some icon. Ideally should be provided.
                long id = categoryRepository.insertCategory(othersCategory);
                newCategoryId = (int) id;
            } else {
                newCategoryId = othersCategory.getId();
            }

            // 3. Reassign transactions
            transactionRepository.updateCategoryForTransactions(category.getId(), newCategoryId);
        }

        // 4. Delete the category
        categoryRepository.deleteCategory(category);
        loadCategories();
    }

    public void refresh() {
        loadCategories();
    }
}
