package edu.birzeit.a1220775_1221026_courseproject.repository;

import android.content.Context;
import android.content.SharedPreferences;

import edu.birzeit.a1220775_1221026_courseproject.data.DatabaseHelper;
import edu.birzeit.a1220775_1221026_courseproject.data.User;

public class UserRepository {
    private DatabaseHelper databaseHelper;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "LoginPrefs";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_REMEMBER_ME = "remember_me";
    private static final String KEY_CURRENT_USER = "current_user";

    public UserRepository(Context context) {
        databaseHelper = DatabaseHelper.getInstance(context);
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public User getUserByEmailAndPassword(String email, String password) {
        return databaseHelper.getUserByEmailAndPassword(email, password);
    }

    public User getUserByEmail(String email) {
        return databaseHelper.getUserByEmail(email);
    }

    public void insertUser(User user) {
        databaseHelper.insertUser(user);
    }

    public void updateUser(User user) {
        databaseHelper.updateUser(user.getEmail(), user.getFirstName(), user.getLastName(), user.getPassword());
    }

    public boolean emailExists(String email) {
        return databaseHelper.emailExists(email);
    }

    public void saveEmailIfRemembered(String email, boolean rememberMe) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (rememberMe) {
            editor.putString(KEY_EMAIL, email);
            editor.putBoolean(KEY_REMEMBER_ME, true);
        } else {
            editor.remove(KEY_EMAIL);
            editor.putBoolean(KEY_REMEMBER_ME, false);
        }
        editor.apply();
    }

    public String getSavedEmail() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        if (rememberMe) {
            return sharedPreferences.getString(KEY_EMAIL, "");
        }
        return "";
    }

    public void clearPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }

    public void setCurrentUser(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_CURRENT_USER, email);
        editor.apply();
    }

    public String getCurrentUser() {
        return sharedPreferences.getString(KEY_CURRENT_USER, "");
    }
}
