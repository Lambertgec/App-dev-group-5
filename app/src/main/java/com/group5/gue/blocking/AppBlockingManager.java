package com.group5.gue.blocking;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class AppBlockingManager {
    private static final String TAG = "AppBlockingManager";
    private static final String PREFS_NAME = "app_blocking_prefs";
    private static final String KEY_BLOCKED_APPS = "blocked_apps";
    private static final String KEY_BLOCKING_ENABLED = "blocking_enabled";

    private final Context context;
    private final SharedPreferences prefs;

    public AppBlockingManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void startBlockingService() {
        Intent intent = new Intent(context, AppBlockingService.class);
        try {
            ContextCompat.startForegroundService(context, intent);
            setBlockingEnabled(true);
            Log.d(TAG, "App blocking service started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start blocking service", e);
        }
    }

    public void stopBlockingService() {
        Intent intent = new Intent(context, AppBlockingService.class);
        context.stopService(intent);
        setBlockingEnabled(false);
        Log.d(TAG, "App blocking service stopped");
    }

    public void addBlockedApp(String packageName) {
        Set<String> blockedApps = getBlockedApps();
        blockedApps.add(packageName);
        saveBlockedApps(blockedApps);
        Log.d(TAG, "Added blocked app: " + packageName);
    }

    public Set<String> getBlockedApps() {
        return new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));
    }

    public boolean isAppBlocked(String packageName) {
        return getBlockedApps().contains(packageName);
    }


    public boolean isBlockingEnabled() {
        return prefs.getBoolean(KEY_BLOCKING_ENABLED, false);
    }

    private void setBlockingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BLOCKING_ENABLED, enabled).apply();
    }

    private void saveBlockedApps(Set<String> blockedApps) {
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, blockedApps).apply();
    }

    public boolean isServiceRunning() {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        java.util.List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            if (AppBlockingService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
