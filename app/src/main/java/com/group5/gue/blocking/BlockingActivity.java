package com.group5.gue.blocking;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.group5.gue.MainActivity;
import com.group5.gue.databinding.ActivityBlockingBinding;

/**
 * BlockingActivity is a full-screen overlay activity designed to restrict user access to 
 * other applications during a lecture. It acts as a "lock screen" that prevents 
 * multitasking and distraction.
 * 
 * <p>This activity uses special window flags to appear over other apps and
 * stay active, and it explicitly disables back navigation.</p>
 *
 */
public class BlockingActivity extends AppCompatActivity {

    /** View binding instance for accessing layout components. */
    private ActivityBlockingBinding binding;

    /**
     * Called when the activity is starting. Configures window flags for persistent 
     * visibility and sets up the layout and navigation restrictions.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure system window flags to show the activity over the lock screen,
        // dismiss the keyguard if possible, and keep the screen active.
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        // Inflate the view binding for this activity
        binding = ActivityBlockingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Intercept hardware back gesture and do nothing, keeping the user in this activity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true)
        {
            /**
             * Handles the back button press. This implementation is intentionally
             * empty to prevent the user from backing out of the blocking screen.
             */
            @Override
            public void handleOnBackPressed() {
            }
        });

        // The UI back button is the only sanctioned exit, navigating to MainActivity
        binding.backButton.setOnClickListener(v -> {
            // Create intent to launch MainActivity
            Intent intent = new Intent(this, MainActivity.class);
            // Flags to clear the task stack and bring MainActivity to the front
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // Start the activity
            startActivity(intent);
            // Close the current blocking activity
            finish();
        });
    }

    /**
     * This is called if a new intent is delivered while the activity is at the top
     * of the stack.
     * 
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }
}
