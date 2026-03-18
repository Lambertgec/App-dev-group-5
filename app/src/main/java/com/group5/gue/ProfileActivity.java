package com.group5.gue;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Button;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.group5.gue.data.user.UserRepository;
import com.group5.gue.data.model.User;
import com.group5.gue.data.Result;

public class ProfileActivity extends AppCompatActivity {

    private UserRepository userRepository;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        userRepository = UserRepository.Companion.getInstance();
        
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.user, new ProfileFragment()).commit();
        }
        
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        loadProfileData();
        setupChangeUsernameButton();
    }

    private void loadProfileData() {
        User cachedUser = userRepository.getCachedUser();
        if (cachedUser != null) {
            displayUserProfile(cachedUser);
        }
    }

    private void displayUserProfile(User user) {
        this.currentUser = user;
        
        TextView usernameView = findViewById(R.id.profileUsername);
        TextView scoreView = findViewById(R.id.profileScore);
        TextView userIdView = findViewById(R.id.profileUserId);
        TextView roleView = findViewById(R.id.profileRole);

        if (usernameView != null) {
            usernameView.setText(user.getName() != null ? user.getName() : "Not set");
        }
        if (scoreView != null) {
            scoreView.setText(String.valueOf(user.getScore()));
        }
        if (userIdView != null) {
            userIdView.setText(user.getId());
        }
        if (roleView != null) {
            roleView.setText(user.getRole().toString());
        }
    }

    private void setupChangeUsernameButton() {
        Button changeUsernameButton = findViewById(R.id.changeUsernameButton);
        if (changeUsernameButton != null) {
            changeUsernameButton.setOnClickListener(v -> showChangeUsernameDialog());
        }
    }

    private void showChangeUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change Username");
        builder.setMessage("Enter new username:");

        final EditText input = new EditText(this);
        input.setText(currentUser != null && currentUser.getName() != null ? currentUser.getName() : "");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newUsername = input.getText().toString().trim();
            if (!newUsername.isEmpty() && currentUser != null) {
                updateUsername(newUsername);
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void updateUsername(String newUsername) {
        if (currentUser == null) return;
        
        User updatedUser = new User(
            currentUser.getId(),
            newUsername,
            currentUser.getScore(),
            currentUser.isAdmin()
        );
        
        userRepository.updateUser(updatedUser, result -> {
            if (result instanceof Result.Success) {
                currentUser = ((Result.Success<User>) result).getData();
                displayUserProfile(currentUser);
                Toast.makeText(
                    ProfileActivity.this,
                    "Username updated successfully",
                    Toast.LENGTH_SHORT
                ).show();
            } else if (result instanceof Result.Error) {
                Exception error = ((Result.Error<User>) result).getError();
                Toast.makeText(
                    ProfileActivity.this,
                    "Failed to update username: " + error.getMessage(),
                    Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    public static class ProfileFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}