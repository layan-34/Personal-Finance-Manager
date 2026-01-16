package edu.birzeit.a1220775_1221026_courseproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import edu.birzeit.a1220775_1221026_courseproject.databinding.ActivityLoginBinding;
import edu.birzeit.a1220775_1221026_courseproject.ui.login.LoginViewModel;
import edu.birzeit.a1220775_1221026_courseproject.ui.login.LoginViewModelFactory;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize ViewModel
        LoginViewModelFactory factory = new LoginViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(LoginViewModel.class);

        // Load saved email or email from SignUpActivity
        String emailFromSignUp = getIntent().getStringExtra("email");
        if (emailFromSignUp != null && !emailFromSignUp.isEmpty()) {
            binding.etEmail.setText(emailFromSignUp);
        } else {
            viewModel.getSavedEmail().observe(this, new Observer<String>() {
                @Override
                public void onChanged(String email) {
                    if (email != null && !email.isEmpty()) {
                        binding.etEmail.setText(email);
                        binding.cbRememberMe.setChecked(true);
                    }
                }
            });
        }

        // Observe login result
        viewModel.getLoginResult().observe(this, new Observer<LoginViewModel.LoginResult>() {
            @Override
            public void onChanged(LoginViewModel.LoginResult result) {
                if (result != null && result.isSuccess()) {
                    Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                    
                    if (result.getMessage().contains("Registration")) {
                        // Clear password field for registration
                        binding.etPassword.setText("");
                        showSuccess(result.getMessage());
                    } else {
                        // Navigate to MainActivity on sign in success
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        });

        // Observe error messages
        viewModel.getErrorMessage().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String error) {
                if (error != null && !error.isEmpty()) {
                    showError(error);
                }
            }
        });

        // Sign In button click listener
        binding.btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();
                boolean rememberMe = binding.cbRememberMe.isChecked();
                viewModel.signIn(email, password, rememberMe);
            }
        });

        // Sign Up button click listener - Navigate to SignUpActivity
        binding.btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showError(String message) {
        binding.tvError.setText(message);
        binding.tvError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        binding.tvError.setVisibility(View.VISIBLE);
    }

    private void showSuccess(String message) {
        binding.tvError.setText(message);
        binding.tvError.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        binding.tvError.setVisibility(View.VISIBLE);
        
        // Hide success message after 3 seconds
        binding.tvError.postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.tvError.setVisibility(View.GONE);
            }
        }, 3000);
    }
}

