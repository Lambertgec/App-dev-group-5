package com.group5.gue;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.group5.gue.blocking.AppBlockingManager;
import com.group5.gue.data.PermissionHandler;
import com.group5.gue.data.auth.AuthManager;
import com.group5.gue.notifications.NotificationScheduler;
import com.group5.gue.ui.login.launcher.LauncherActivity;

import com.group5.gue.databinding.ActivityMainBinding;

import kotlin.Unit;

/**
 * MainActivity is the primary container for the student user experience.
 * It manages the main application lifecycle, navigation between core fragments, 
 * background attendance synchronization, and notification scheduling.
 * 
 * <p>Key Responsibilities:
 * <ul>
 *   <li>Initializing background WorkManager tasks for attendance.</li>
 *   <li>Handling fragment transitions via the BottomNavigationView.</li>
 *   <li>Managing system permissions for Calendar and Notifications.</li>
 *   <li>Configuring notification channels for lecture alerts.</li>
 *   <li>Providing a top-level options menu for Profile, Settings, and Logout.</li>
 * </ul>
 * </p>
 */
public class MainActivity extends AppCompatActivity {
    
    /** 
     * Work request to check attendance status.
     * Ensures consistent tracking even if the app is in the background.
     */
    PeriodicWorkRequest attendanceWork =
            new PeriodicWorkRequest.Builder(AttendanceCheckWorker.class, 15, TimeUnit.MINUTES)
                    .build();

    // View binding instance for accessing layout components.
    ActivityMainBinding binding;

    /**
     * Initializes the activity, sets up the UI, handles permissions, and starts background work.
     * 
     * @param savedInstanceState State bundle for restoration.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        
        // Setup the custom Toolbar as the Action Bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Prepare notifications and permissions
        createNotificationChannels();
        // Request necessary permissions
        requestPermissions();

        // Restore previously selected calendar from local storage
        String savedCalendar = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("selected_calendar", null);

        if (savedCalendar != null) {
            CalendarHandler.selectedCalendar = savedCalendar;
        }

        // Schedule the periodic attendance check task
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "attendance_check",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                attendanceWork
        );

        // Load the default landing fragment
        switchFragmant(new HomeFragment());

        // Handle edge-to-edge system bar insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup listener for bottom navigation menu clicks
        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
           if ((menuItem.getItemId()) == R.id.home) {
                switchFragmant(new HomeFragment());
           } else if ((menuItem.getItemId()) == R.id.map) {
               switchFragmant(new MapFragment());
           } else if ((menuItem.getItemId()) == R.id.friends) {
               switchFragmant(new FriendsFragment());
           }else {
               switchFragmant(new LeaderboardFragment());
           }
           return true;
        });

        // Trigger notification scheduling logic
        scheduleNotifications();

        // Handle possible deep link intents
        handleIntent(getIntent());
    }

    /**
     * Configures notification channels required for high-importance alerts on Android 8.0+.
     */
    private void createNotificationChannels() {
        // Channel creation only needed for API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                // Channel for general lecture reminders
                NotificationChannel lectureChannel = new NotificationChannel(
                        "lecture_channel",
                        "Lecture Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                // Channel for location-based proximity alerts
                NotificationChannel proximityChannel = new NotificationChannel(
                        "proximity_channel",
                        "Proximity Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                // Register channels with the system
                manager.createNotificationChannel(lectureChannel);
                manager.createNotificationChannel(proximityChannel);
            }
        }
    }

    /**
     * Requests runtime permissions for Calendar and Notifications.
     */
    private void requestPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.READ_CALENDAR);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        ArrayList<String> listToRequest = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listToRequest.add(perm);
            }
        }

        if (!listToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, listToRequest.toArray(new String[0]), 101);
        }
    }

    /**
     * Iterates through future calendar events and schedules relevant local notifications.
     */
    private void scheduleNotifications() {
        if (CalendarHandler.selectedCalendar != null && 
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {

            CalendarHandler handler = new CalendarHandler(getContentResolver());
            handler.setCalendar(CalendarHandler.selectedCalendar);

            ArrayList<Event> events = handler.getFutureEvents();

            for (Event event : events) {
                NotificationScheduler.scheduleNotification(this, event);         // 30-min alarm
                NotificationScheduler.scheduleProximityNotification(this, event); // 10-min alarm
                NotificationScheduler.scheduleCatchUp(this, event);              // missed catch-up
            }
        }
    }

    /**
     * Callback for new intents, used for handling deep links while the activity is running.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    /**
     * Parses the incoming intent for navigation markers.
     */
    private void handleIntent(Intent intent) {
        if (intent != null) {
            String openTab = intent.getStringExtra("open_tab");
            if ("map".equals(openTab)) {
                switchFragmant(new MapFragment());
                binding.bottomNavigationView.setSelectedItemId(R.id.map);
            } else if ("home".equals(openTab)) {
                switchFragmant(new HomeFragment());
                binding.bottomNavigationView.setSelectedItemId(R.id.home);
            }
        }
    }

    /**
     * Initializes the options menu in the top Action Bar.
     *
     * @return true to display the menu.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // P populate the top menu
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.top_menu, menu);

        // Configure logic for Profile menu item
        MenuItem profile = menu.findItem(R.id.profile);
        profile.setOnMenuItemClickListener(item -> {
            // Start the profile view activity
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        });

        // Configure logic for Settings menu item
        MenuItem setting = menu.findItem(R.id.setting);
        setting.setOnMenuItemClickListener(item -> {
            // Start the settings preference activity
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        });

        // Configure logic for Logout menu item
        MenuItem logout = menu.findItem(R.id.logout);
        logout.setOnMenuItemClickListener(item -> {
            // Initiate logout
            AuthManager.Companion.getInstance(MainActivity.this).logout(result -> {
               // On successful logout, return to the Launcher screen
               Intent intent = new Intent(MainActivity.this, LauncherActivity.class);
               startActivity(intent);
               finish(); // Close main activity on logout
               return Unit.INSTANCE;
            });
            return true;
        });
        return true;
    }

    /**
     * Helper method to replace the current fragment in the main container.
     * 
     * @param fragment The new Fragment instance to display.
     */
    private void switchFragmant(Fragment fragment){
        // Obtain fragment manager and start transaction
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // Replace current fragment with the new one
        fragmentTransaction.replace(R.id.frame_one, fragment);
        fragmentTransaction.commit();
    }
}
