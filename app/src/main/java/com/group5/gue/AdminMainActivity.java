package com.group5.gue;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.group5.gue.databinding.ActivityAdminMainBinding;

/**
 * AdminMainActivity is the primary activity for users with administrative privileges.
 * It provides a dashboard layout with a bottom navigation menu for switching
 * between administrative fragments like Home, Map, Friends, and Leaderboard.
 * 
 * <p>This activity utilizes EdgeToEdge display for a modern UI experience and 
 * ViewBinding for safe interaction with its components.</p>
 *
 */
public class AdminMainActivity extends AppCompatActivity {

    /** Binding instance for the activity_admin_main layout. */
    private ActivityAdminMainBinding binding;

    /**
     * Initializes the activity, sets up EdgeToEdge display, and configures the 
     * bottom navigation and initial fragment.
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());

        // Enable edge-to-edge layout for immersive UI
        EdgeToEdge.enable(this);

        setContentView(binding.getRoot());

        setSupportActionBar(binding.adminToolbar);

        // Load Admin Home by default
        switchFragment(new AdminHomeFragment());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set listener for bottom navigation menu item selections
        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            int itemId = menuItem.getItemId();
            
            // Navigate to different fragments based on the selected menu item
            if (itemId == R.id.home) {
                switchFragment(new AdminHomeFragment());
            } else if (itemId == R.id.map) {
                switchFragment(new MapFragment());
            } else if (itemId == R.id.friends) {
                switchFragment(new FriendsFragment());
            } else {
                switchFragment(new LeaderboardFragment());
            }
            return true;
        });
    }

    /**
     * Helper method to perform fragment transactions within the admin frame.
     * Replaces the currently displayed fragment with the provided new fragment.
     * 
     * @param fragment The new Fragment instance to display.
     */
    public void switchFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.admin_frame, fragment);
        fragmentTransaction.commit();
    }
}
