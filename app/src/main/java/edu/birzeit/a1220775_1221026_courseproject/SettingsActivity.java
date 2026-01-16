package edu.birzeit.a1220775_1221026_courseproject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchDarkMode;
    private Spinner spinnerPeriod;
    private Button btnManageCategories;
    private View layoutManageCategories;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

        switchDarkMode = findViewById(R.id.switchDarkMode);
        spinnerPeriod = findViewById(R.id.spinnerPeriod);
        btnManageCategories = findViewById(R.id.btnManageCategories);
        layoutManageCategories = findViewById(R.id.layoutManageCategories);

        setupThemeSwitch();
        setupPeriodSpinner();
        setupCategoriesButton();
    }

    private void setupThemeSwitch() {
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        switchDarkMode.setChecked(isDarkMode);

        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("dark_mode", isChecked);
            editor.apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });
    }

    private void setupPeriodSpinner() {
        String[] displayPeriods = { "Day", "Week", "Month" };
        String[] internalValues = { "Daily", "Weekly", "Monthly" };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, displayPeriods);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPeriod.setAdapter(adapter);

        String currentPeriod = prefs.getString("default_period", "Daily");
        int position = 0; // Default Day (Daily)
        if (currentPeriod.equals("Weekly"))
            position = 1;
        else if (currentPeriod.equals("Monthly"))
            position = 2;

        spinnerPeriod.setSelection(position);

        spinnerPeriod.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = internalValues[position];
                prefs.edit().putString("default_period", selected).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupCategoriesButton() {
        View.OnClickListener listener = v -> {
            Intent intent = new Intent(SettingsActivity.this, CategoriesActivity.class);
            startActivity(intent);
        };

        btnManageCategories.setOnClickListener(listener);
        if (layoutManageCategories != null) {
            layoutManageCategories.setOnClickListener(listener);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
