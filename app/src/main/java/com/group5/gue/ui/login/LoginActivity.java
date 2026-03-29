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
 */
public class LoginActivity extends AppCompatActivity {

    // Binding object to access UI components in activity_login.xml.
    private ActivityLoginBinding binding;
    // Manager responsible for authentication operations with Supabase.
    private AuthManager authManager;

    /**
     * Initializes the login screen, setting up view binding and click listeners.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
     * Attempts to sign in the user using the provided email and password.
     */
    private void doSignIn() {
        binding.loading.setVisibility(View.VISIBLE);
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        authManager.signInWithEmail(email, password, result -> {
            binding.loading.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                // Return success to the calling activity (usually LauncherActivity)
                setResult(Activity.RESULT_OK);
                finish();
            } else {
                showError(result);
            }
        });

    }

    /**
     * Attempts to register a new user account with email and password.
     */
    private void doSignUp() {
        binding.loading.setVisibility(View.VISIBLE);
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        authManager.signUpWithEmail(email, password, result -> {
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
     * Initiates the Google Sign-In flow via the AuthManager.
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
     * 
     * @param result The result object containing error details.
     */
    private void showError(Result<?> result) {
        String message = getString(R.string.login_failed);
        if (result instanceof Result.Error) {
            Exception error = ((Result.Error<?>) result).getError();
            if (error != null && error.getMessage() != null) {
                message = error.getMessage();
            }
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}