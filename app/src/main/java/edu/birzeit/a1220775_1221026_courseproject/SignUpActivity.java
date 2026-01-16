package edu.birzeit.a1220775_1221026_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import com.google.android.material.textfield.TextInputLayout;

import edu.birzeit.a1220775_1221026_courseproject.databinding.ActivitySignupBinding;
import edu.birzeit.a1220775_1221026_courseproject.ui.signup.SignUpViewModel;
import edu.birzeit.a1220775_1221026_courseproject.ui.signup.SignUpViewModelFactory;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private SignUpViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        SignUpViewModelFactory factory = new SignUpViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(SignUpViewModel.class);

        // Observe sign up result
        viewModel.getSignUpResult().observe(this, new Observer<SignUpViewModel.SignUpResult>() {
            @Override
            public void onChanged(SignUpViewModel.SignUpResult result) {
                if (result != null && result.isSuccess()) {
                    Toast.makeText(SignUpActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    // Navigate back to LoginActivity
                    Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                    intent.putExtra("email", binding.etEmail.getText().toString().trim());
                    startActivity(intent);
                    finish();
                }
            }
        });

        // Observe field errors (all errors at once)
        viewModel.getFieldErrors().observe(this, new Observer<List<SignUpViewModel.FieldError>>() {
            @Override
            public void onChanged(List<SignUpViewModel.FieldError> errors) {
                if (errors != null && !errors.isEmpty()) {
                    for (SignUpViewModel.FieldError error : errors) {
                        showFieldError(error);
                    }
                }
            }
        });

        // Create Account button click listener
        binding.btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllErrors();
                String email = binding.etEmail.getText().toString().trim();
                String firstName = binding.etFirstName.getText().toString().trim();
                String lastName = binding.etLastName.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();
                String confirmPassword = binding.etConfirmPassword.getText().toString().trim();
                
                viewModel.signUp(email, firstName, lastName, password, confirmPassword);
            }
        });

        // Back to Login button click listener
        binding.btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void showFieldError(SignUpViewModel.FieldError error) {
        TextInputLayout targetLayout = null;
        
        switch (error.getFieldType()) {
            case SignUpViewModel.FieldError.EMAIL:
                targetLayout = binding.tilEmail;
                break;
            case SignUpViewModel.FieldError.FIRST_NAME:
                targetLayout = binding.tilFirstName;
                break;
            case SignUpViewModel.FieldError.LAST_NAME:
                targetLayout = binding.tilLastName;
                break;
            case SignUpViewModel.FieldError.PASSWORD:
                targetLayout = binding.tilPassword;
                break;
            case SignUpViewModel.FieldError.CONFIRM_PASSWORD:
                targetLayout = binding.tilConfirmPassword;
                break;
        }
        
        if (targetLayout != null) {
            targetLayout.setError(error.getErrorMessage());
            targetLayout.setErrorEnabled(true);
            // Set red border and text color for error state
            targetLayout.setBoxStrokeErrorColor(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
            targetLayout.setErrorTextColor(ContextCompat.getColorStateList(this, android.R.color.holo_red_dark));
        }
    }

    private void clearAllErrors() {
        clearError(binding.tilEmail);
        clearError(binding.tilFirstName);
        clearError(binding.tilLastName);
        clearError(binding.tilPassword);
        clearError(binding.tilConfirmPassword);
    }

    private void clearError(TextInputLayout layout) {
        layout.setErrorEnabled(false);
        layout.setError(null);
    }
}

