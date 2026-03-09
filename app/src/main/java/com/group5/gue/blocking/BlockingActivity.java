package com.group5.gue.blocking;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.group5.gue.MainActivity;
import com.group5.gue.databinding.ActivityBlockingBinding;

public class BlockingActivity extends AppCompatActivity {

    private ActivityBlockingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // draw over other apps
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        binding = ActivityBlockingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // back button sends to GUE
        binding.backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        });
    }

    // helps with retriggering the activity
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    // disables back button
    @Override
    public void onBackPressed() {
    }
}