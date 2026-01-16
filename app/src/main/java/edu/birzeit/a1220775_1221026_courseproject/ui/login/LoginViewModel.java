package edu.birzeit.a1220775_1221026_courseproject.ui.login;

import android.app.Application;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.birzeit.a1220775_1221026_courseproject.data.User;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class LoginViewModel extends AndroidViewModel {
    private UserRepository repository;
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private MutableLiveData<String> savedEmail = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
        loadSavedEmail();
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<String> getSavedEmail() {
        return savedEmail;
    }

    private void loadSavedEmail() {
        String email = repository.getSavedEmail();
        savedEmail.setValue(email);
    }

    public void signIn(String email, String password, boolean rememberMe) {
        if (!validateInput(email, password)) {
            return;
        }

        User user = repository.getUserByEmailAndPassword(email, password);
        if (user != null) {
            repository.saveEmailIfRemembered(email, rememberMe);
            repository.setCurrentUser(email);
            loginResult.setValue(new LoginResult(true, "Sign in successful!"));
        } else {
            errorMessage.setValue("Invalid email or password");
        }
    }


    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            errorMessage.setValue("Email is required");
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Please enter a valid email address");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            errorMessage.setValue("Password is required");
            return false;
        }

        return true;
    }

    public static class LoginResult {
        private boolean success;
        private String message;

        public LoginResult(boolean success, String message) {
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
}

