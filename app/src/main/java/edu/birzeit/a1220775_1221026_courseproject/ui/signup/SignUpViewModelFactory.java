package edu.birzeit.a1220775_1221026_courseproject.ui.signup;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

public class SignUpViewModelFactory implements ViewModelProvider.Factory {
    private Application application;

    public SignUpViewModelFactory(Application application) {
        this.application = application;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SignUpViewModel.class)) {
            return (T) new SignUpViewModel(application);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}


