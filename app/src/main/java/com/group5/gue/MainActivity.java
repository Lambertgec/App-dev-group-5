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

public class MainActivity extends AppCompatActivity {
    PeriodicWorkRequest attendanceWork =
            new PeriodicWorkRequest.Builder(AttendanceCheckWorker.class, 15, TimeUnit.MINUTES)
                    .build();
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        createNotificationChannels();
        requestPermissions();

        String savedCalendar = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("selected_calendar", null);

        if (savedCalendar != null) {
            CalendarHandler.selectedCalendar = savedCalendar;
        }

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "attendance_check",
                androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                attendanceWork
        );

        switchFragmant(new HomeFragment());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main),
                (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

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

        scheduleNotifications();

        handleIntent(getIntent());
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                NotificationChannel lectureChannel = new NotificationChannel(
                        "lecture_channel",
                        "Lecture Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                NotificationChannel proximityChannel = new NotificationChannel(
                        "proximity_channel",
                        "Proximity Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                manager.createNotificationChannel(lectureChannel);
                manager.createNotificationChannel(proximityChannel);
            }
        }
    }

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

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.top_menu, menu);

        MenuItem profile = menu.findItem(R.id.profile);
        profile.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            return true;
        });

        MenuItem setting = menu.findItem(R.id.setting);
        setting.setOnMenuItemClickListener(item -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        });

        MenuItem logout = menu.findItem(R.id.logout);
        logout.setOnMenuItemClickListener(item -> {
            AuthManager.Companion.getInstance(MainActivity.this).logout(result -> {
               Intent intent = new Intent(MainActivity.this, LauncherActivity.class);
               startActivity(intent);
               return Unit.INSTANCE;

            });
            return true;
        });
        return true;
    }

    private void switchFragmant(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_one, fragment);
        fragmentTransaction.commit();
    }
}