package com.group5.gue.blocking;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.group5.gue.MainActivity;
import com.group5.gue.databinding.ActivityBlockingBinding;

public class BlockingActivity extends AppCompatActivity {

    private ActivityBlockingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        binding = ActivityBlockingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Intercept hardware back gesture and do nothing, keeping the user in this activity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            @Override
            public void handleOnBackPressed() {
                // Intentionally empty — back navigation is disabled
            }
        });

        // The UI back button is the only sanctioned exit, navigating to MainActivity
        binding.backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}