package edu.birzeit.a1220775_1221026_courseproject.repository;

import android.content.Context;
import android.database.Cursor;

import edu.birzeit.a1220775_1221026_courseproject.data.DatabaseHelper;
import edu.birzeit.a1220775_1221026_courseproject.data.Transaction;

import java.util.List;

public class TransactionRepository {
    private DatabaseHelper databaseHelper;

    public TransactionRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    public long insertTransaction(Transaction transaction) {
        return databaseHelper.insertTransaction(
                transaction.getUserEmail(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategoryId(),
                transaction.getDescription());
    }

    public void updateTransaction(Transaction transaction) {
        databaseHelper.updateTransaction(
                transaction.getId(),
                transaction.getUserEmail(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getDate(),
                transaction.getCategoryId(),
                transaction.getDescription());
    }

    public void deleteTransaction(Transaction transaction) {
        databaseHelper.deleteTransaction(transaction.getId());
    }

    public void deleteTransactionById(int transactionId) {
        databaseHelper.deleteTransaction(transactionId);
    }

    public List<Transaction> getTransactionsByUser(String userEmail, String sortOrder) {
        Cursor cursor = databaseHelper.getTransactionsByUser(userEmail, null, null, null, sortOrder);
        return mapCursorToTransactionList(cursor);
    }

    public List<Transaction> getTransactionsByUser(String userEmail) {
        return getTransactionsByUser(userEmail, "DESC");
    }

    public List<Transaction> getTransactionsByUserAndType(String userEmail, String type) {
        Cursor cursor = databaseHelper.getTransactionsByUser(userEmail, type, null, null, "DESC");
        return mapCursorToTransactionList(cursor);
    }

    public List<Transaction> getTransactionsByUserAndDateRange(String userEmail, long startDate, long endDate) {
        Cursor cursor = databaseHelper.getTransactionsByUser(userEmail, null, startDate, endDate, "DESC");
        return mapCursorToTransactionList(cursor);
    }

    public Transaction getTransactionById(int transactionId) {
        Cursor cursor = databaseHelper.getTransactionById(transactionId);
        if (cursor != null && cursor.moveToFirst()) {
            Transaction transaction = mapCursorToTransaction(cursor);
            cursor.close();
            return transaction;
        }
        if (cursor != null)
            cursor.close();
        return null;
    }

    public Double getTotalByUserAndType(String userEmail, String type) {
        return databaseHelper.getTotalByUserAndType(userEmail, type);
    }

    public Double getTotalByUserTypeAndDateRange(String userEmail, String type, long startDate, long endDate) {
        return databaseHelper.getTotalByUserTypeAndDateRange(userEmail, type, startDate, endDate);
    }

    public int countTransactionsByCategory(int categoryId) {
        return databaseHelper.countTransactionsByCategory(categoryId);
    }

    public void updateCategoryForTransactions(int oldCategoryId, int newCategoryId) {
        databaseHelper.updateCategoryForTransactions(oldCategoryId, newCategoryId);
    }

    private List<Transaction> mapCursorToTransactionList(Cursor cursor) {
        List<Transaction> list = new java.util.ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                list.add(mapCursorToTransaction(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        if (cursor != null)
            cursor.close();
        return list;
    }

    private Transaction mapCursorToTransaction(Cursor cursor) {
        Transaction t = new Transaction();
        t.setId(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        t.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow("user_email")));
        t.setType(cursor.getString(cursor.getColumnIndexOrThrow("type")));
        t.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow("amount")));
        t.setDate(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
        t.setCategoryId(cursor.getInt(cursor.getColumnIndexOrThrow("category_id")));
        t.setDescription(cursor.getString(cursor.getColumnIndexOrThrow("description")));
        return t;
    }
}
