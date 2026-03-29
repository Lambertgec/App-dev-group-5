package com.group5.gue;

import android.content.Intent;
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
import com.group5.gue.ui.login.launcher.LauncherActivity;

/**
 * Activity for viewing and managing the user's profile.
 * Allows users to view their score, change their username, and delete their account.
 */
public class ProfileActivity extends AppCompatActivity {

    // Repository for user-related data operations.
    private UserRepository userRepository;
    // The currently logged-in user's data.
    private User currentUser;

    /**
     * Initializes the profile activity and its components.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        userRepository = UserRepository.Companion.getInstance();
        
        // Host the profile fragment if not already present
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.user, new ProfileFragment()).commit();
        }
        
        // Setup navigation back button
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        
        loadProfileData();
        setupButtons();
    }

    /**
     * Retrieves the user data from the local cache.
     */
    private void loadProfileData() {
        User cachedUser = userRepository.getCachedUser();
        if (cachedUser != null) {
            displayUserProfile(cachedUser);
        }
    }

    /**
     * Updates the UI widgets with the provided user information.
     * 
     * @param user The user object containing the data to display.
     */
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

    /**
     * Configures the click listeners for action buttons.
     */
    private void setupButtons() {
        Button changeUsernameButton = findViewById(R.id.changeUsernameButton);
        if (changeUsernameButton != null) {
            changeUsernameButton.setOnClickListener(v -> showChangeUsernameDialog());
        }

        Button deleteAccountButton = findViewById(R.id.deleteAccountButton);
        if (deleteAccountButton != null) {
            deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog());
        }
    }

    /**
     * Displays a dialog to input a new username.
     */
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

    /**
     * Displays a confirmation dialog before deleting the user account.
     */
    private void showDeleteAccountDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Account");
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.");

        builder.setPositiveButton("Delete", (dialog, which) -> {
            if (currentUser != null) {
                // Request account deletion from the repository
                userRepository.deleteAccount(result -> {
                    runOnUiThread(() -> {
                        if (result instanceof Result.Success) {
                            Toast.makeText(
                                ProfileActivity.this,
                                "Account deleted successfully",
                                Toast.LENGTH_SHORT
                            ).show();
                            // Redirect to landing page on success
                            startActivity(new Intent(ProfileActivity.this, LauncherActivity.class));
                            finishAffinity();
                        } else if (result instanceof Result.Error) {
                            Exception error = ((Result.Error<Void>) result).getError();
                            Toast.makeText(
                                ProfileActivity.this,
                                "Failed to delete account: " + error.getMessage(),
                                Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                    return kotlin.Unit.INSTANCE;
                });
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    /**
     * Sends the updated username to the repository and updates local state.
     * 
     * @param newUsername The new display name for the user.
     */
    private void updateUsername(String newUsername) {
        if (currentUser == null) return;
        
        User updatedUser = new User(
            currentUser.getId(),
            newUsername,
            currentUser.getScore(),
            currentUser.isAdmin()
        );
        
        userRepository.updateUser(updatedUser, result -> {
            runOnUiThread(() -> {
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
        });
    }

    /**
     * Fragment placeholder for profile settings.
     */
    public static class ProfileFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            //setPreferencesFromResource(R.xml.root_preferences, rootKey);
        }
    }
}