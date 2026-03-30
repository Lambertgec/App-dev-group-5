package com.group5.gue.ui.login;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group5.gue.MainActivity;
import com.group5.gue.R;
import com.group5.gue.data.Result;
import com.group5.gue.data.auth.AuthManager;
import com.group5.gue.databinding.ActivityLoginBinding;

/**
 * LoginActivity handles the user interface for authentication.
 * It provides fields for email and password, and supports signing in with
 * email/password or via Google Sign-In.
 * This activity acts as an entry point for users to access their accounts.
 */
public class LoginActivity extends AppCompatActivity {

    // Binding object to access UI components in activity_login.xml.
    private ActivityLoginBinding binding;
    // Manager responsible for authentication operations with Supabase.
    private AuthManager authManager;

    /**
     * Initializes the login screen, setting up view binding and click listeners.
     * @param savedInstanceState If the activity is being re-initialized then this Bundle contains the most recent data.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate the layout using View Binding to avoid findViewById calls
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain an instance of AuthManager for handling auth logic
        authManager = AuthManager.Companion.getInstance(this);

        // Allow users to submit the form using the 'Done' key on their keyboard
        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doSignIn();
            }
            return false;
        });

        // Set up click listeners for the various authentication actions
        binding.login.setOnClickListener(v -> doSignIn());
        binding.signUp.setOnClickListener(v -> doSignUp());
        binding.loginGoogle.setOnClickListener(v -> doGoogleSignIn());
    }

    /**
     * Attempts to sign in the user using the provided email and password credentials.
     * Shows a loading indicator during the network request.
     */
    private void doSignIn() {
        binding.loading.setVisibility(View.VISIBLE);
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        // Delegate sign-in logic to AuthManager
        authManager.signInWithEmail(email, password, result -> {
            // Hide loading indicator regardless of outcome
            binding.loading.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                // Return success to the calling activity (usually LauncherActivity)
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                // Display error details to the user
                showError(result);
            }
        });

    }

    /**
     * Attempts to register a new user account with the provided email and password.
     * Validations are performed on the backend via AuthManager.
     */
    private void doSignUp() {
        binding.loading.setVisibility(View.VISIBLE);
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        // Delegate sign-up logic to AuthManager
        authManager.signUpWithEmail(email, password, result -> {
            binding.loading.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                // Treat successful sign-up same as successful login for user flow
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                showError(result);
            }
        });
    }

    /**
     * Initiates the Google OAuth Sign-In flow via the AuthManager.
     * This may launch an external browser or system dialog for account selection.
     */
    private void doGoogleSignIn() {
        binding.loading.setVisibility(View.VISIBLE);

        authManager.signInWithGoogle(result -> {
            binding.loading.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                showError(result);
            }
        });
    }

    /**
     * Displays an error message to the user when authentication fails.
     * Extracts the error message from the Result object if available.
     * 
     * @param result The result object containing error details.
     */
    private void showError(Result<?> result) {
        // Default generic error message
        String message = getString(R.string.login_failed);
        if (result instanceof Result.Error) {
            Exception error = ((Result.Error<?>) result).getError();
            // Override with specific message from exception if provided
            if (error != null && error.getMessage() != null) {
                message = error.getMessage();
            }
        }
        // Show the error toast to the user
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}