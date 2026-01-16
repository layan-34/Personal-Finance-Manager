package edu.birzeit.a1220775_1221026_courseproject.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "personal_finance_database.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USERS = "users";
    private static final String TABLE_CATEGORIES = "categories";
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String TABLE_BUDGETS = "budgets";
    private static final String TABLE_GOALS = "goals";

    // Users Table Columns
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_PASSWORD_HASH = "password_hash";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Categories Table Columns
    private static final String COLUMN_CATEGORY_ID = "id";
    private static final String COLUMN_CATEGORY_USER_EMAIL = "user_email";
    private static final String COLUMN_CATEGORY_TYPE = "type";
    private static final String COLUMN_CATEGORY_NAME = "name";
    private static final String COLUMN_CATEGORY_COLOR = "color";
    private static final String COLUMN_CATEGORY_ICON = "icon";

    // Transactions Table Columns
    private static final String COLUMN_TRANSACTION_ID = "id";
    private static final String COLUMN_TRANSACTION_USER_EMAIL = "user_email";
    private static final String COLUMN_TRANSACTION_TYPE = "type";
    private static final String COLUMN_TRANSACTION_AMOUNT = "amount";
    private static final String COLUMN_TRANSACTION_DATE = "date";
    private static final String COLUMN_TRANSACTION_CATEGORY_ID = "category_id";
    private static final String COLUMN_TRANSACTION_DESCRIPTION = "description";

    // Budgets Table Columns
    private static final String COLUMN_BUDGET_ID = "id";
    private static final String COLUMN_BUDGET_USER_EMAIL = "user_email";
    private static final String COLUMN_BUDGET_CATEGORY_ID = "category_id";
    private static final String COLUMN_BUDGET_MONTH = "month";
    private static final String COLUMN_BUDGET_LIMIT_AMOUNT = "limit_amount";
    private static final String COLUMN_BUDGET_ALERT_RATIO = "alert_ratio";

    // Goals Table Columns
    private static final String COLUMN_GOAL_ID = "id";
    private static final String COLUMN_GOAL_USER_EMAIL = "user_email";
    private static final String COLUMN_GOAL_NAME = "name";
    private static final String COLUMN_GOAL_TARGET_AMOUNT = "target_amount";
    private static final String COLUMN_GOAL_CURRENT_AMOUNT = "current_amount";
    private static final String COLUMN_GOAL_TARGET_DATE = "target_date";
    private static final String COLUMN_GOAL_STATUS = "status";

    // Create Table SQL Queries
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
            + COLUMN_EMAIL + " TEXT PRIMARY KEY,"
            + COLUMN_FIRST_NAME + " TEXT NOT NULL,"
            + COLUMN_LAST_NAME + " TEXT NOT NULL,"
            + COLUMN_PASSWORD_HASH + " TEXT NOT NULL,"
            + COLUMN_CREATED_AT + " INTEGER"
            + ")";

    private static final String CREATE_CATEGORIES_TABLE = "CREATE TABLE " + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_CATEGORY_USER_EMAIL + " TEXT NOT NULL,"
            + COLUMN_CATEGORY_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_CATEGORY_TYPE + " IN ('INCOME','EXPENSE')),"
            + COLUMN_CATEGORY_NAME + " TEXT NOT NULL,"
            + COLUMN_CATEGORY_COLOR + " TEXT,"
            + COLUMN_CATEGORY_ICON + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_CATEGORY_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL
            + ") ON DELETE CASCADE,"
            + "UNIQUE(" + COLUMN_CATEGORY_USER_EMAIL + ", " + COLUMN_CATEGORY_TYPE + ", " + COLUMN_CATEGORY_NAME + ")"
            + ")";

    private static final String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
            + COLUMN_TRANSACTION_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_TRANSACTION_USER_EMAIL + " TEXT NOT NULL,"
            + COLUMN_TRANSACTION_TYPE + " TEXT NOT NULL CHECK(" + COLUMN_TRANSACTION_TYPE + " IN ('INCOME','EXPENSE')),"
            + COLUMN_TRANSACTION_AMOUNT + " REAL NOT NULL,"
            + COLUMN_TRANSACTION_DATE + " INTEGER NOT NULL,"
            + COLUMN_TRANSACTION_CATEGORY_ID + " INTEGER NOT NULL,"
            + COLUMN_TRANSACTION_DESCRIPTION + " TEXT,"
            + "FOREIGN KEY(" + COLUMN_TRANSACTION_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL
            + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_TRANSACTION_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "("
            + COLUMN_CATEGORY_ID + ") ON DELETE RESTRICT"
            + ")";

    private static final String CREATE_BUDGETS_TABLE = "CREATE TABLE " + TABLE_BUDGETS + "("
            + COLUMN_BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_BUDGET_USER_EMAIL + " TEXT NOT NULL,"
            + COLUMN_BUDGET_CATEGORY_ID + " INTEGER NOT NULL,"
            + COLUMN_BUDGET_MONTH + " INTEGER NOT NULL,"
            + COLUMN_BUDGET_LIMIT_AMOUNT + " REAL NOT NULL,"
            + COLUMN_BUDGET_ALERT_RATIO + " REAL DEFAULT 0.5,"
            + "FOREIGN KEY(" + COLUMN_BUDGET_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL
            + ") ON DELETE CASCADE,"
            + "FOREIGN KEY(" + COLUMN_BUDGET_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID
            + ") ON DELETE CASCADE,"
            + "UNIQUE(" + COLUMN_BUDGET_USER_EMAIL + ", " + COLUMN_BUDGET_CATEGORY_ID + ", " + COLUMN_BUDGET_MONTH + ")"
            + ")";

    private static final String CREATE_GOALS_TABLE = "CREATE TABLE " + TABLE_GOALS + "("
            + COLUMN_GOAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_GOAL_USER_EMAIL + " TEXT NOT NULL,"
            + COLUMN_GOAL_NAME + " TEXT NOT NULL,"
            + COLUMN_GOAL_TARGET_AMOUNT + " REAL NOT NULL,"
            + COLUMN_GOAL_CURRENT_AMOUNT + " REAL DEFAULT 0,"
            + COLUMN_GOAL_TARGET_DATE + " INTEGER,"
            + COLUMN_GOAL_STATUS + " TEXT DEFAULT 'ACTIVE',"
            + "FOREIGN KEY(" + COLUMN_GOAL_USER_EMAIL + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_EMAIL
            + ") ON DELETE CASCADE"
            + ")";

    // Create Indexes
    private static final String CREATE_INDEX_TX_USER_DATE = "CREATE INDEX idx_tx_user_date ON " + TABLE_TRANSACTIONS
            + "(" + COLUMN_TRANSACTION_USER_EMAIL + ", " + COLUMN_TRANSACTION_DATE + ")";

    private static final String CREATE_INDEX_TX_USER_TYPE = "CREATE INDEX idx_tx_user_type ON " + TABLE_TRANSACTIONS
            + "(" + COLUMN_TRANSACTION_USER_EMAIL + ", " + COLUMN_TRANSACTION_TYPE + ")";

    // Singleton instance
    private static DatabaseHelper instance;

    private DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);

        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Enable foreign keys
        db.execSQL("PRAGMA foreign_keys = ON;");

        // Create tables
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_CATEGORIES_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        db.execSQL(CREATE_BUDGETS_TABLE);
        db.execSQL(CREATE_GOALS_TABLE);

        // Create indexes
        db.execSQL(CREATE_INDEX_TX_USER_DATE);
        db.execSQL(CREATE_INDEX_TX_USER_TYPE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GOALS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Create tables again
        onCreate(db);
    }

    // ==================== USERS TABLE METHODS ====================

    public long insertUser(String email, String firstName, String lastName, String passwordHash, Long createdAt) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_PASSWORD_HASH, passwordHash);
        if (createdAt != null) {
            values.put(COLUMN_CREATED_AT, createdAt);
        } else {
            values.put(COLUMN_CREATED_AT, System.currentTimeMillis());
        }

        long result = db.insert(TABLE_USERS, null, values);
        return result;
    }

    public long insertUser(User user) {
        return insertUser(user.getEmail(), user.getFirstName(), user.getLastName(),
                user.getPassword(), System.currentTimeMillis());
    }

    public User getUserByEmailAndPassword(String email, String passwordHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] columns = {
                COLUMN_EMAIL,
                COLUMN_FIRST_NAME,
                COLUMN_LAST_NAME,
                COLUMN_PASSWORD_HASH
        };

        String selection = COLUMN_EMAIL + " = ? AND " + COLUMN_PASSWORD_HASH + " = ?";
        String[] selectionArgs = { email, passwordHash };

        Cursor cursor = db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1");

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return user;
    }

    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        User user = null;

        String[] columns = {
                COLUMN_EMAIL,
                COLUMN_FIRST_NAME,
                COLUMN_LAST_NAME,
                COLUMN_PASSWORD_HASH
        };

        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1");

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PASSWORD_HASH)));
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return user;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;

        String[] columns = { COLUMN_EMAIL };
        String selection = COLUMN_EMAIL + " = ?";
        String[] selectionArgs = { email };

        Cursor cursor = db.query(
                TABLE_USERS,
                columns,
                selection,
                selectionArgs,
                null,
                null,
                null,
                "1");

        if (cursor != null && cursor.getCount() > 0) {
            exists = true;
        }

        if (cursor != null) {
            cursor.close();
        }
        return exists;
    }

    public int updateUser(String email, String firstName, String lastName, String passwordHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FIRST_NAME, firstName);
        values.put(COLUMN_LAST_NAME, lastName);
        values.put(COLUMN_PASSWORD_HASH, passwordHash);

        String whereClause = COLUMN_EMAIL + " = ?";
        String[] whereArgs = { email };

        int rowsAffected = db.update(TABLE_USERS, values, whereClause, whereArgs);
        return rowsAffected;
    }

    public int deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_EMAIL + " = ?";
        String[] whereArgs = { email };

        int rowsAffected = db.delete(TABLE_USERS, whereClause, whereArgs);
        return rowsAffected;
    }

    // ==================== CATEGORIES TABLE METHODS ====================

    public long insertCategory(String userEmail, String type, String name, String color, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_CATEGORY_USER_EMAIL, userEmail);
        values.put(COLUMN_CATEGORY_TYPE, type);
        values.put(COLUMN_CATEGORY_NAME, name);
        if (color != null) {
            values.put(COLUMN_CATEGORY_COLOR, color);
        }
        if (icon != null) {
            values.put(COLUMN_CATEGORY_ICON, icon);
        }

        long result = db.insert(TABLE_CATEGORIES, null, values);
        return result;
    }

    public Cursor getCategoryById(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_CATEGORIES,
                null,
                COLUMN_CATEGORY_ID + " = ?",
                new String[] { String.valueOf(categoryId) },
                null,
                null,
                null,
                "1");
    }

    public Cursor getCategoriesByUser(String userEmail, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_CATEGORY_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };

        if (type != null) {
            selection += " AND " + COLUMN_CATEGORY_TYPE + " = ?";
            selectionArgs = new String[] { userEmail, type };
        }

        return db.query(
                TABLE_CATEGORIES,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_CATEGORY_NAME + " ASC");
    }

    public int deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_CATEGORY_ID + " = ?";
        String[] whereArgs = { String.valueOf(categoryId) };

        int rowsAffected = db.delete(TABLE_CATEGORIES, whereClause, whereArgs);
        db.close();
        return rowsAffected;
    }

    public int updateCategory(int id, String userEmail, String type, String name, String color, String icon) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_USER_EMAIL, userEmail);
        values.put(COLUMN_CATEGORY_TYPE, type);
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_CATEGORY_COLOR, color);
        values.put(COLUMN_CATEGORY_ICON, icon);
        return db.update(TABLE_CATEGORIES, values, COLUMN_CATEGORY_ID + " = ?", new String[] { String.valueOf(id) });
    }

    public long insertTransaction(String userEmail, String type, double amount, long date, int categoryId,
            String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TRANSACTION_USER_EMAIL, userEmail);
        values.put(COLUMN_TRANSACTION_TYPE, type);
        values.put(COLUMN_TRANSACTION_AMOUNT, amount);
        values.put(COLUMN_TRANSACTION_DATE, date);
        values.put(COLUMN_TRANSACTION_CATEGORY_ID, categoryId);
        if (description != null) {
            values.put(COLUMN_TRANSACTION_DESCRIPTION, description);
        }

        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        return result;
    }

    public Cursor getTransactionById(int transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_TRANSACTIONS,
                null,
                COLUMN_TRANSACTION_ID + " = ?",
                new String[] { String.valueOf(transactionId) },
                null,
                null,
                null,
                "1");
    }

    public Cursor getTransactionsByUser(String userEmail, String type, Long startDate, Long endDate, String sortOrder) {
        SQLiteDatabase db = this.getReadableDatabase();
        StringBuilder selection = new StringBuilder(COLUMN_TRANSACTION_USER_EMAIL + " = ?");
        java.util.ArrayList<String> selectionArgsList = new java.util.ArrayList<>();
        selectionArgsList.add(userEmail);

        if (type != null) {
            selection.append(" AND ").append(COLUMN_TRANSACTION_TYPE).append(" = ?");
            selectionArgsList.add(type);
        }

        if (startDate != null) {
            selection.append(" AND ").append(COLUMN_TRANSACTION_DATE).append(" >= ?");
            selectionArgsList.add(String.valueOf(startDate));
        }

        if (endDate != null) {
            selection.append(" AND ").append(COLUMN_TRANSACTION_DATE).append(" <= ?");
            selectionArgsList.add(String.valueOf(endDate));
        }

        return db.query(
                TABLE_TRANSACTIONS,
                null,
                selection.toString(),
                selectionArgsList.toArray(new String[0]),
                null,
                null,
                COLUMN_TRANSACTION_DATE + ("ASC".equalsIgnoreCase(sortOrder) ? " ASC" : " DESC"));
    }

    public Double getTotalByUserAndType(String userEmail, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_TRANSACTION_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TRANSACTION_USER_EMAIL + " = ? AND " + COLUMN_TRANSACTION_TYPE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { userEmail, type });
        Double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public Double getTotalByUserTypeAndDateRange(String userEmail, String type, long startDate, long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_TRANSACTION_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TRANSACTION_USER_EMAIL + " = ? AND " + COLUMN_TRANSACTION_TYPE + " = ?" +
                " AND " + COLUMN_TRANSACTION_DATE + " >= ? AND " + COLUMN_TRANSACTION_DATE + " <= ?";
        Cursor cursor = db.rawQuery(query,
                new String[] { userEmail, type, String.valueOf(startDate), String.valueOf(endDate) });
        Double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public Double getSpentByCategoryAndDateRange(String userEmail, int categoryId, long startDate, long endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_TRANSACTION_AMOUNT + ") FROM " + TABLE_TRANSACTIONS +
                " WHERE " + COLUMN_TRANSACTION_USER_EMAIL + " = ? AND " + COLUMN_TRANSACTION_CATEGORY_ID + " = ?" +
                " AND " + COLUMN_TRANSACTION_DATE + " >= ? AND " + COLUMN_TRANSACTION_DATE + " <= ?" +
                " AND " + COLUMN_TRANSACTION_TYPE + " = 'EXPENSE'";
        Cursor cursor = db.rawQuery(query, new String[] { userEmail, String.valueOf(categoryId),
                String.valueOf(startDate), String.valueOf(endDate) });
        Double total = 0.0;
        if (cursor != null && cursor.moveToFirst()) {
            total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public int countTransactionsByCategory(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_TRANSACTIONS + " WHERE " + COLUMN_TRANSACTION_CATEGORY_ID
                + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(categoryId) });
        int count = 0;
        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public void updateCategoryForTransactions(int oldCategoryId, int newCategoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_CATEGORY_ID, newCategoryId);
        db.update(TABLE_TRANSACTIONS, values, COLUMN_TRANSACTION_CATEGORY_ID + " = ?",
                new String[] { String.valueOf(oldCategoryId) });
    }

    public int updateTransaction(int id, String userEmail, String type, double amount, long date, int categoryId,
            String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TRANSACTION_USER_EMAIL, userEmail);
        values.put(COLUMN_TRANSACTION_TYPE, type);
        values.put(COLUMN_TRANSACTION_AMOUNT, amount);
        values.put(COLUMN_TRANSACTION_DATE, date);
        values.put(COLUMN_TRANSACTION_CATEGORY_ID, categoryId);
        values.put(COLUMN_TRANSACTION_DESCRIPTION, description);
        return db.update(TABLE_TRANSACTIONS, values, COLUMN_TRANSACTION_ID + " = ?",
                new String[] { String.valueOf(id) });
    }

    public int deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_TRANSACTION_ID + " = ?";
        String[] whereArgs = { String.valueOf(transactionId) };

        int rowsAffected = db.delete(TABLE_TRANSACTIONS, whereClause, whereArgs);
        return rowsAffected;
    }

    // ==================== BUDGETS TABLE METHODS ====================

    public long insertBudget(String userEmail, int categoryId, int month, double limitAmount, Double alertRatio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BUDGET_USER_EMAIL, userEmail);
        values.put(COLUMN_BUDGET_CATEGORY_ID, categoryId);
        values.put(COLUMN_BUDGET_MONTH, month);
        values.put(COLUMN_BUDGET_LIMIT_AMOUNT, limitAmount);
        if (alertRatio != null) {
            values.put(COLUMN_BUDGET_ALERT_RATIO, alertRatio);
        } else {
            values.put(COLUMN_BUDGET_ALERT_RATIO, 0.5);
        }

        long result = db.insert(TABLE_BUDGETS, null, values);
        return result;
    }

    public Cursor getBudgetById(int budgetId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_BUDGETS,
                null,
                COLUMN_BUDGET_ID + " = ?",
                new String[] { String.valueOf(budgetId) },
                null,
                null,
                null,
                "1");
    }

    public Cursor getBudgetsByUser(String userEmail, Integer month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_BUDGET_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };

        if (month != null) {
            selection += " AND " + COLUMN_BUDGET_MONTH + " = ?";
            selectionArgs = new String[] { userEmail, String.valueOf(month) };
        }

        return db.query(
                TABLE_BUDGETS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_BUDGET_MONTH + " DESC");
    }

    public int updateBudget(int budgetId, double limitAmount, Double alertRatio) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_BUDGET_LIMIT_AMOUNT, limitAmount);
        if (alertRatio != null) {
            values.put(COLUMN_BUDGET_ALERT_RATIO, alertRatio);
        }

        String whereClause = COLUMN_BUDGET_ID + " = ?";
        String[] whereArgs = { String.valueOf(budgetId) };

        int rowsAffected = db.update(TABLE_BUDGETS, values, whereClause, whereArgs);
        return rowsAffected;
    }

    public int deleteBudget(int budgetId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_BUDGET_ID + " = ?";
        String[] whereArgs = { String.valueOf(budgetId) };

        int rowsAffected = db.delete(TABLE_BUDGETS, whereClause, whereArgs);
        return rowsAffected;
    }

    // ==================== GOALS TABLE METHODS ====================

    public long insertGoal(String userEmail, String name, double targetAmount, Double currentAmount, Long targetDate,
            String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_GOAL_USER_EMAIL, userEmail);
        values.put(COLUMN_GOAL_NAME, name);
        values.put(COLUMN_GOAL_TARGET_AMOUNT, targetAmount);
        if (currentAmount != null) {
            values.put(COLUMN_GOAL_CURRENT_AMOUNT, currentAmount);
        } else {
            values.put(COLUMN_GOAL_CURRENT_AMOUNT, 0);
        }
        if (targetDate != null) {
            values.put(COLUMN_GOAL_TARGET_DATE, targetDate);
        }
        if (status != null) {
            values.put(COLUMN_GOAL_STATUS, status);
        } else {
            values.put(COLUMN_GOAL_STATUS, "ACTIVE");
        }

        long result = db.insert(TABLE_GOALS, null, values);
        return result;
    }

    public Cursor getGoalById(int goalId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_GOALS,
                null,
                COLUMN_GOAL_ID + " = ?",
                new String[] { String.valueOf(goalId) },
                null,
                null,
                null,
                "1");
    }

    public Cursor getGoalsByUser(String userEmail, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        String selection = COLUMN_GOAL_USER_EMAIL + " = ?";
        String[] selectionArgs = { userEmail };

        if (status != null) {
            selection += " AND " + COLUMN_GOAL_STATUS + " = ?";
            selectionArgs = new String[] { userEmail, status };
        }

        return db.query(
                TABLE_GOALS,
                null,
                selection,
                selectionArgs,
                null,
                null,
                COLUMN_GOAL_TARGET_DATE + " ASC");
    }

    public int updateGoal(int goalId, Double currentAmount, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        if (currentAmount != null) {
            values.put(COLUMN_GOAL_CURRENT_AMOUNT, currentAmount);
        }
        if (status != null) {
            values.put(COLUMN_GOAL_STATUS, status);
        }

        String whereClause = COLUMN_GOAL_ID + " = ?";
        String[] whereArgs = { String.valueOf(goalId) };

        int rowsAffected = db.update(TABLE_GOALS, values, whereClause, whereArgs);
        return rowsAffected;
    }

    public int deleteGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_GOAL_ID + " = ?";
        String[] whereArgs = { String.valueOf(goalId) };

        int rowsAffected = db.delete(TABLE_GOALS, whereClause, whereArgs);
        return rowsAffected;
    }
}
