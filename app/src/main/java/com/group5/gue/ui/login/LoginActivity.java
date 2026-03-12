package com.group5.gue.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.group5.gue.R;
import com.group5.gue.data.Result;
import com.group5.gue.data.auth.AuthManager;
import com.group5.gue.databinding.ActivityLoginBinding;
import com.group5.gue.ui.login.launcher.LauncherActivity;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthManager authManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        authManager = AuthManager.Companion.getInstance(this);

        binding.password.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                doSignIn();
            }
            return false;
        });

        binding.login.setOnClickListener(v -> doSignIn());
        binding.signUp.setOnClickListener(v -> doSignUp());
        binding.loginGoogle.setOnClickListener(v -> doGoogleSignIn());
    }

    private void doSignIn() {
        binding.loading.setVisibility(View.VISIBLE);
        String email = binding.email.getText().toString();
        String password = binding.password.getText().toString();

        authManager.signInWithEmail(email, password, result -> {
            binding.loading.setVisibility(View.GONE);
            if (result instanceof Result.Success) {
                setResult(Activity.RESULT_OK);
                Intent intent = new Intent(getApplicationContext(), LauncherActivity.class);
                startActivity(intent);
                finish();
            } else {
                showError(result);
            }
        });
    }

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