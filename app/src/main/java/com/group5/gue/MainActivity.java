package com.group5.gue;

import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.widget.Toolbar;

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
import com.group5.gue.data.attendance.AttendanceRepository;
import com.group5.gue.data.auth.AuthManager;
import com.group5.gue.data.model.AttendanceRecord;
import com.group5.gue.ui.login.launcher.LauncherActivity;

import com.group5.gue.databinding.ActivityMainBinding;

import kotlin.Unit;

public class MainActivity extends AppCompatActivity {
    PeriodicWorkRequest attendanceWork =
            new PeriodicWorkRequest.Builder(AttendanceCheckWorker.class, 15, TimeUnit.MINUTES)
                    .build();
    ActivityMainBinding binding;
    private AppBlockingManager blockingManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        blockingManager = new AppBlockingManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.top_menu, menu);

        if (blockingManager.isBlockingEnabled()) {
            PermissionHandler permissionHandler = new PermissionHandler(this);
            permissionHandler.requestAppBlocking();

            blockingManager.addBlockedApp("com.android.chrome");
            blockingManager.addBlockedApp("com.google.android.youtube");
            blockingManager.addBlockedApp("app.revanced.android.youtube");
            blockingManager.addBlockedApp("com.instagram.android");
            blockingManager.addBlockedApp("com.facebook.katana");
            blockingManager.addBlockedApp("com.facebook.orca");
            blockingManager.addBlockedApp("com.facebook.lite");
            blockingManager.addBlockedApp("com.facebook.mlite");
            blockingManager.addBlockedApp("com.discord");

            try {
                blockingManager.startBlockingService();
            } catch (Exception e) {
                Toast.makeText(this, "Error starting app blocking: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "App blocking is disabled", Toast.LENGTH_SHORT).show();
        }

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