package edu.birzeit.a1220775_1221026_courseproject.ui.profile;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import edu.birzeit.a1220775_1221026_courseproject.data.User;
import edu.birzeit.a1220775_1221026_courseproject.repository.UserRepository;

public class ProfileViewModel extends AndroidViewModel {

    private final UserRepository repository;
    private final MutableLiveData<User> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> updateResult = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        repository = new UserRepository(application);
    }

    public LiveData<User> getUser() {
        return userLiveData;
    }

    public LiveData<String> getUpdateResult() {
        return updateResult;
    }

    public void loadUserProfile() {
        String email = repository.getCurrentUser();
        if (email != null && !email.isEmpty()) {
            new LoadUserTask(repository, userLiveData, updateResult).execute(email);
        } else {
            updateResult.setValue("No logged-in user found");
            userLiveData.setValue(null);
        }
    }

    public void updateUser(User user) {
        new UpdateUserTask(repository, updateResult).execute(user);
    }

    private static class LoadUserTask extends AsyncTask<String, Void, User> {
        private final UserRepository repository;
        private final MutableLiveData<User> userLiveData;
        private final MutableLiveData<String> updateResult;

        LoadUserTask(UserRepository repository, MutableLiveData<User> userLiveData,
                MutableLiveData<String> updateResult) {
            this.repository = repository;
            this.userLiveData = userLiveData;
            this.updateResult = updateResult;
        }

        @Override
        protected User doInBackground(String... emails) {
            return repository.getUserByEmail(emails[0]);
        }

        @Override
        protected void onPostExecute(User user) {
            userLiveData.setValue(user);
            if (user == null) {
                updateResult.setValue("User profile not found");
            }
        }
    }

    private static class UpdateUserTask extends AsyncTask<User, Void, Boolean> {
        private UserRepository repository;
        private MutableLiveData<String> updateResult;

        UpdateUserTask(UserRepository repository, MutableLiveData<String> updateResult) {
            this.repository = repository;
            this.updateResult = updateResult;
        }

        @Override
        protected Boolean doInBackground(User... users) {
            try {
                repository.updateUser(users[0]);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean success) {
            if (success) {
                updateResult.setValue("Profile updated successfully");
            } else {
                updateResult.setValue("Failed to update profile");
            }
        }
    }
}
