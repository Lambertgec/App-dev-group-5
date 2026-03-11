package com.group5.gue.ui.login;

import android.app.Activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.group5.gue.MainActivity;
import com.group5.gue.R;
import com.group5.gue.data.Result;
import com.group5.gue.data.auth.AuthRepository;
import com.group5.gue.databinding.ActivityLoginBinding;
import com.group5.gue.ui.login.launcher.LauncherActivity;


public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthRepository authRepository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authRepository = AuthRepository.getInstance(this);
        final EditText usernameEditText = binding.email;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final Button signUpButton = binding.signUp;
        final Button googleButton = binding.loginGoogle;
        final ProgressBar loadingProgressBar = binding.loading;

        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signInWithEmail(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString(), loadingProgressBar);
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                signInWithEmail(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), loadingProgressBar);
            }
        });

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                signUpWithEmail(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString(), loadingProgressBar);
            }
        });

        googleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                authRepository.signInWithGoogle(result -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    if (result instanceof Result.Success) {
                        startActivity(new Intent(LoginActivity.this, LauncherActivity.class));
                        setResult(Activity.RESULT_OK);
                        finish();
                    } else if (result instanceof Result.Error) {
                        Result.Error<?> error = (Result.Error<?>) result;
                        String message = error.getError() != null
                                ? error.getError().getMessage()
                                : getString(R.string.login_failed);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        showLoginFailed(R.string.login_failed);
                    }
                });
            }
        });

        final Button signupButton = binding.signUp;
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                setResult(Activity.RESULT_OK);
                finish();
            }
        });

    }

    private void signInWithEmail(String email, String password, ProgressBar loadingProgressBar) {
        authRepository.signInWithEmail(email, password, result -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                startActivity(new Intent(LoginActivity.this, LauncherActivity.class));
                setResult(Activity.RESULT_OK);
                finish();
            } else if (result instanceof Result.Error) {
                Result.Error<?> error = (Result.Error<?>) result;
                String message = error.getError() != null
                        ? error.getError().getMessage()
                        : getString(R.string.login_failed);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                showLoginFailed(R.string.login_failed);
            }
        });
    }

    private void signUpWithEmail(String email, String password, ProgressBar loadingProgressBar) {
        authRepository.signUpWithEmail(email, password, result -> {
            loadingProgressBar.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                if (authRepository.isLoggedIn()) {
                    startActivity(new Intent(LoginActivity.this, LauncherActivity.class));
                    setResult(Activity.RESULT_OK);
                    finish();
                    return;
                }
                Toast.makeText(getApplicationContext(), "Sign up successful. Please sign in.", Toast.LENGTH_SHORT).show();
            } else if (result instanceof Result.Error) {
                Result.Error<?> error = (Result.Error<?>) result;
                String message = error.getError() != null
                        ? error.getError().getMessage()
                        : getString(R.string.login_failed);
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            } else {
                showLoginFailed(R.string.login_failed);
            }
        });
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}