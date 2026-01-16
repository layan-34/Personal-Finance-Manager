package edu.birzeit.a1220775_1221026_courseproject.ui.signup;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import edu.birzeit.a1220775_1221026_courseproject.data.User;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class SignUpViewModel extends AndroidViewModel {
    private UserRepository repository;
    private MutableLiveData<SignUpResult> signUpResult = new MutableLiveData<>();
    private MutableLiveData<List<FieldError>> fieldErrors = new MutableLiveData<>();

    public SignUpViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<SignUpResult> getSignUpResult() {
        return signUpResult;
    }

    public LiveData<List<FieldError>> getFieldErrors() {
        return fieldErrors;
    }

    public void signUp(String email, String firstName, String lastName, 
                      String password, String confirmPassword) {
        
        // Validate all fields and collect all errors
        List<FieldError> errors = new ArrayList<>();
        boolean isValid = true;
        
        // Validate Email
        if (TextUtils.isEmpty(email)) {
            errors.add(new FieldError(FieldError.EMAIL, "Email is required"));
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errors.add(new FieldError(FieldError.EMAIL, "Please enter a valid email address"));
            isValid = false;
        } else if (repository.emailExists(email)) {
            errors.add(new FieldError(FieldError.EMAIL, "User with this email already exists"));
            isValid = false;
        }
        
        // Validate First Name
        if (TextUtils.isEmpty(firstName)) {
            errors.add(new FieldError(FieldError.FIRST_NAME, "First name is required"));
            isValid = false;
        } else if (firstName.length() < 3 || firstName.length() > 10) {
            errors.add(new FieldError(FieldError.FIRST_NAME, "First name must be between 3 and 10 characters"));
            isValid = false;
        }
        
        // Validate Last Name
        if (TextUtils.isEmpty(lastName)) {
            errors.add(new FieldError(FieldError.LAST_NAME, "Last name is required"));
            isValid = false;
        } else if (lastName.length() < 3 || lastName.length() > 10) {
            errors.add(new FieldError(FieldError.LAST_NAME, "Last name must be between 3 and 10 characters"));
            isValid = false;
        }
        
        // Validate Password
        if (TextUtils.isEmpty(password)) {
            errors.add(new FieldError(FieldError.PASSWORD, "Password is required"));
            isValid = false;
        } else if (password.length() < 6 || password.length() > 12) {
            errors.add(new FieldError(FieldError.PASSWORD, "Password must be between 6 and 12 characters"));
            isValid = false;
        } else if (!isValidPassword(password)) {
            errors.add(new FieldError(FieldError.PASSWORD, "Password must contain at least one number, one lowercase, and one uppercase letter"));
            isValid = false;
        }
        
        // Validate Confirm Password
        if (TextUtils.isEmpty(confirmPassword)) {
            errors.add(new FieldError(FieldError.CONFIRM_PASSWORD, "Please confirm your password"));
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            errors.add(new FieldError(FieldError.CONFIRM_PASSWORD, "Passwords do not match"));
            isValid = false;
        }
        
        if (!isValid) {
            fieldErrors.setValue(errors);
            return;
        }
        
        // All validations passed, create user
        User newUser = new User(email, firstName, lastName, password);
        repository.insertUser(newUser);
        signUpResult.setValue(new SignUpResult(true, "Registration successful! Please sign in."));
    }

    private boolean isValidPassword(String password) {
        boolean hasNumber = false;
        boolean hasLowercase = false;
        boolean hasUppercase = false;
        
        for (char c : password.toCharArray()) {
            if (Character.isDigit(c)) {
                hasNumber = true;
            } else if (Character.isLowerCase(c)) {
                hasLowercase = true;
            } else if (Character.isUpperCase(c)) {
                hasUppercase = true;
            }
        }
        
        return hasNumber && hasLowercase && hasUppercase;
    }

    public static class SignUpResult {
        private boolean success;
        private String message;

        public SignUpResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class FieldError {
        public static final int EMAIL = 1;
        public static final int FIRST_NAME = 2;
        public static final int LAST_NAME = 3;
        public static final int PASSWORD = 4;
        public static final int CONFIRM_PASSWORD = 5;

        private int fieldType;
        private String errorMessage;

        public FieldError(int fieldType, String errorMessage) {
            this.fieldType = fieldType;
            this.errorMessage = errorMessage;
        }

        public int getFieldType() {
            return fieldType;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

