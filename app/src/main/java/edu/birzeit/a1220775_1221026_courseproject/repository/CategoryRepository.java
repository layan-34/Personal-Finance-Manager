package edu.birzeit.a1220775_1221026_courseproject.repository;

import android.content.Context;
import android.database.Cursor;

import edu.birzeit.a1220775_1221026_courseproject.data.Category;
import edu.birzeit.a1220775_1221026_courseproject.data.DatabaseHelper;

import java.util.List;

public class CategoryRepository {
    private DatabaseHelper databaseHelper;

    public CategoryRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public long insertCategory(Category category) {
        return databaseHelper.insertCategory(
                category.getUserEmail(),
                category.getType(),
                category.getName(),
                category.getColor(),
                category.getIcon());
    }

    public void updateCategory(Category category) {
        // Implementation for updateCategory in DatabaseHelper is missing color/icon
        // update,
        // but it doesn't exist in original DAOs either or it was simple update.
        // For now, if we need it, we can add it to DatabaseHelper.
        databaseHelper.updateCategory(
                category.getId(),
                category.getUserEmail(),
                category.getType(),
                category.getName(),
                category.getColor(),
                category.getIcon());
    }

    public void deleteCategory(Category category) {
        databaseHelper.deleteCategory(category.getId());
    }

    public void deleteCategoryById(int categoryId) {
        databaseHelper.deleteCategory(categoryId);
    }

    public List<Category> getCategoriesByUser(String userEmail) {
        Cursor cursor = databaseHelper.getCategoriesByUser(userEmail, null);
        return mapCursorToCategoryList(cursor);
    }

    public List<Category> getCategoriesByUserAndType(String userEmail, String type) {
        Cursor cursor = databaseHelper.getCategoriesByUser(userEmail, type);
        return mapCursorToCategoryList(cursor);
    }

    public Category getCategoryById(int categoryId) {
        Cursor cursor = databaseHelper.getCategoryById(categoryId);
        if (cursor != null && cursor.moveToFirst()) {
            Category category = mapCursorToCategory(cursor);
            cursor.close();
            return category;
        }
        if (cursor != null)
            cursor.close();
        return null;
    }

    public Category getCategoryByNameAndType(String userEmail, String name, String type) {
        // Direct DatabaseHelper doesn't have this, let's implement via getCategories
        List<Category> categories = getCategoriesByUserAndType(userEmail, type);
        if (categories != null) {
            for (Category c : categories) {
                if (c.getName().equalsIgnoreCase(name))
                    return c;
            }
        }
        return null;
    }

    private List<Category> mapCursorToCategoryList(Cursor cursor) {
        List<Category> list = new java.util.ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapCursorToCategory(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        if (cursor != null)
            cursor.close();
        return list;
    }

    private Category mapCursorToCategory(Cursor cursor) {
        Category category = new Category();
        category.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        category.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
        category.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        category.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        category.setColor(cursor.getString(cursor.getColumnIndexOrThrow("color")));
        category.setIcon(cursor.getString(cursor.getColumnIndexOrThrow("icon")));
        return category;
    }
}
