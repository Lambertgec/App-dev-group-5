package com.group5.gue.blocking;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

/**
 * AppBlockingManager acts as the central control unit for the application blocking feature.
 * It manages the list of restricted packages, handles the lifecycle of the background
 * AppBlockingService, and interacts with SharedPreferences to persist blocking states.
 * 
 * <p>This manager maintains a default list of commonly distracting applications 
 * (browsers, social media, entertainment) that are restricted during attendance sessions.</p>
 *
 */
public class AppBlockingManager {
    /** Tag used for identifying log entries from this manager. */
    private static final String TAG = "AppBlockingManager";
    
    /** Shared preferences file dedicated to blocking configurations. */
    public static final String PREFS_NAME = "app_blocking_prefs";
    
    /** Key used to store the set of blocked package names in preferences. */
    private static final String KEY_BLOCKED_APPS = "blocked_apps";
    
    /** Key for the boolean preference that enables or disables the blocking feature. */
    public static final String KEY_BLOCKING_ENABLED = "blocking";

    /** Context required for accessing system services and shared preferences. */
    private final Context context;
    
    /** SharedPreferences instance for persisting blocking data. */
    private final SharedPreferences prefs;

    /**
     * Constructs the AppBlockingManager and initializes the default list of blocked apps.
     *
     * @param context The context used to initialize internal states and preferences.
     */
    public AppBlockingManager(Context context) {
        // Store the provided context
        this.context = context;
        // Initialize preferences using the private mode for security
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Pre-populate the blocklist with common distractions to ensure immediate functionality
        this.addBlockedApp("com.android.chrome");
        this.addBlockedApp("com.google.android.youtube");
        this.addBlockedApp("app.revanced.android.youtube");
        this.addBlockedApp("com.instagram.android");
        this.addBlockedApp("com.facebook.katana");
        this.addBlockedApp("com.facebook.orca");
        this.addBlockedApp("com.facebook.lite");
        this.addBlockedApp("com.facebook.mlite");
        this.addBlockedApp("com.discord");
    }

    /**
     * Checks if the overall app blocking feature is enabled in user settings.
     * 
     * @return true if blocking is toggled ON, false otherwise.
     */
    public boolean isBlockingEnabled() {
        // Read boolean flag from preferences, default to false if not set
        return prefs.getBoolean(KEY_BLOCKING_ENABLED, false);
    }

    /**
     * Starts the foreground AppBlockingService to begin monitoring active applications.
     * Uses ContextCompat to ensure compatibility with modern Android foreground service requirements.
     */
    public void startBlockingService() {
        // Define an intent targeting the background service
        Intent intent = new Intent(context, AppBlockingService.class);
        try {
            // Attempt to start the service in the foreground (requires persistent notification)
            ContextCompat.startForegroundService(context, intent);
            Log.d(TAG, "App blocking service started");
        } catch (Exception e) {
            // Log catastrophic failures (e.g., missing foreground service permissions)
            Log.e(TAG, "Failed to start blocking service", e);
        }
    }

    /**
     * Stops the AppBlockingService and ends all application monitoring activities.
     */
    public void stopBlockingService() {
        // Define intent for the service to be stopped
        Intent intent = new Intent(context, AppBlockingService.class);
        // Explicitly request the system to terminate the service
        context.stopService(intent);
        Log.d(TAG, "App blocking service stopped");
    }

    /**
     * Adds a specific application package to the current blocklist.
     *
     * @param packageName The unique Android package identifier (e.g., "com.twitter.android").
     */
    public void addBlockedApp(String packageName) {
        Set<String> blockedApps = getBlockedApps();
        blockedApps.add(packageName);
        saveBlockedApps(blockedApps);
        Log.d(TAG, "Added blocked app: " + packageName);
    }

    /**
     * Retrieves the set of all package names currently marked as blocked.
     * 
     * @return A Set of package name strings.
     */
    public Set<String> getBlockedApps() {
        return new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));
    }

    /**
     * Verifies if a specific application is currently in the restricted list.
     *
     * @param packageName The package name to verify.
     * @return true if the app is blocked, false otherwise.
     */
    public boolean isAppBlocked(String packageName) {
        return getBlockedApps().contains(packageName);
    }

    /**
     * Private helper to persist the set of blocked applications to SharedPreferences.
     * 
     * @param blockedApps The set of package names to save.
     */
    private void saveBlockedApps(Set<String> blockedApps) {
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, blockedApps).apply();
    }

    /**
     * Checks if the AppBlockingService is currently active and registered in the system.
     * 
     * @return true if the service is found in the running services list, false otherwise.
     */
    public boolean isServiceRunning() {
        // Access the ActivityManager service
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }

        // Iterate through the list of running services (MAX_VALUE ensures we see everything)
        // getRunningServices is deprecated but still works for identifying own services
        java.util.List<ActivityManager.RunningServiceInfo> services = manager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo service : services) {
            // Compare class names to identify our blocking service
            if (AppBlockingService.class.getName().equals(service.service.getClassName())) {
                return true; // Match found
            }
        }
        // No running instance found
        return false;
    }
}
