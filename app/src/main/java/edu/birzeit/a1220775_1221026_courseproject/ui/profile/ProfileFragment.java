package edu.birzeit.a1220775_1221026_courseproject.ui.profile;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import edu.birzeit.a1220775_1221026_courseproject.R;
import edu.birzeit.a1220775_1221026_courseproject.data.User;

public class ProfileFragment extends Fragment {

    private ProfileViewModel profileViewModel;
    private TextInputEditText etFirstName, etLastName, etNewPassword, etConfirmNewPassword;
    private android.widget.ImageView ivProfile;
    private Button btnSaveProfile;
    private FloatingActionButton btnChangePhoto;
    private User currentUser;
    private android.net.Uri selectedImageUri;

    private final androidx.activity.result.ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    ivProfile.setImageURI(uri);
                    if (currentUser != null) {
                        currentUser.setProfileImageUri(uri.toString());
                    }
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getActivity() == null) {
            return root;
        }

        ProfileViewModelFactory factory = new ProfileViewModelFactory(getActivity().getApplication());
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);

        etFirstName = root.findViewById(R.id.etFirstName);
        etLastName = root.findViewById(R.id.etLastName);
        etNewPassword = root.findViewById(R.id.etNewPassword);
        etConfirmNewPassword = root.findViewById(R.id.etConfirmNewPassword);
        btnSaveProfile = root.findViewById(R.id.btnSaveProfile);
        btnChangePhoto = root.findViewById(R.id.btnChangePhoto);
        ivProfile = root.findViewById(R.id.ivProfile);

        btnSaveProfile.setEnabled(false);
        btnChangePhoto.setEnabled(false);

        profileViewModel.loadUserProfile();

        profileViewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                etFirstName.setText(user.getFirstName());
                etLastName.setText(user.getLastName());
                if (user.getProfileImageUri() != null) {
                    try {
                        ivProfile.setImageURI(android.net.Uri.parse(user.getProfileImageUri()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                btnSaveProfile.setEnabled(true);
                btnChangePhoto.setEnabled(true);
            } else {
                btnSaveProfile.setEnabled(false);
                btnChangePhoto.setEnabled(false);
            }
        });

        profileViewModel.getUpdateResult().observe(getViewLifecycleOwner(), message -> {
            if (message != null && getContext() != null) {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePhoto.setOnClickListener(v -> pickImage.launch("image/*"));

        return root;
    }

    private void saveProfile() {
        if (currentUser == null)
            return;

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(firstName)) {
            etFirstName.setError("First name is required");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            etLastName.setError("Last name is required");
            return;
        }

        if (firstName.length() < 3 || firstName.length() > 10) {
            etFirstName.setError("Name must be 3-10 characters");
            return;
        }

        if (lastName.length() < 3 || lastName.length() > 10) {
            etLastName.setError("Name must be 3-10 characters");
            return;
        }

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);

        if (!TextUtils.isEmpty(newPassword)) {
            if (newPassword.length() < 6 || newPassword.length() > 12) {
                etNewPassword.setError("Password must be 6-12 characters");
                return;
            }
            if (!newPassword.matches(".*\\d.*") || !newPassword.matches(".*[a-z].*")
                    || !newPassword.matches(".*[A-Z].*")) {
                etNewPassword.setError("Password must contain 1 number, 1 lowercase, 1 uppercase");
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                etConfirmNewPassword.setError("Passwords do not match");
                return;
            }
            currentUser.setPassword(newPassword);
        }

        profileViewModel.updateUser(currentUser);
    }
}
