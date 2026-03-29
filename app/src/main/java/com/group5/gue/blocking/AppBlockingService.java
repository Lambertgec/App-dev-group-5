package com.group5.gue.blocking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.group5.gue.ProximityChecker;
import com.group5.gue.R;
import com.group5.gue.VerificationCodeFragment;

/**
 * AppBlockingService is a foreground service responsible for monitoring the currently
 * focused application on the device. If a user attempts to use a "blocked" app 
 * this service intercepts the action and launches the BlockingActivity.
 * 
 * <p>The service also monitors the user's physical proximity to the lecture building
 * and the remaining duration of the lecture to automatically disable itself when 
 * the session ends or the user leaves the area.</p>
 *
 */
public class AppBlockingService extends Service {
    
    /** Logging tag for this service. */
    private static final String TAG = "AppBlockingService";
    
    /** Identifier for the notification channel used by this foreground service. */
    private static final String CHANNEL_ID = "AppBlockingChannel";
    
    /** Unique ID for the foreground notification. */
    private static final int NOTIFICATION_ID = 12345;
    
    /** Frequency in milliseconds for checking the foreground application (0.5 seconds). */
    private static final long CHECK_INTERVAL = 500;
    
    /** Frequency in milliseconds for checking physical proximity (30 seconds). */
    private static final long PROXIMITY_CHECK_INTERVAL = 30000; 
    
    /** Name of the shared preferences file containing lecture metadata. */
    private static final String PREFS_LECTURE = "lecture_prefs";

    /** Handler used to schedule periodic monitoring tasks on the main thread. */
    private Handler handler;
    
    /** Runnable task that repeatedly checks the active foreground app. */
    private Runnable checkAppRunnable;
    
    /** Runnable task that repeatedly verifies user proximity to the lecture. */
    private Runnable proximityCheckRunnable;
    
    /** Package name of this application to avoid blocking itself. */
    private String ownPackageName;
    
    /** Manager for checking which apps are currently in the blocklist. */
    private AppBlockingManager blockingManager;
    
    /** Utility for verifying location proximity. */
    private ProximityChecker proximityChecker;

    /**
     * Initializes the service, creates notification channels, and starts monitoring tasks.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Cache local package name
        ownPackageName = getPackageName();
        
        // Initialize dependencies
        blockingManager = new AppBlockingManager(this);
        proximityChecker = new ProximityChecker(this);
        handler = new Handler();
        
        // Android 8+ requires a notification channel for foreground services
        createNotificationChannel();
        
        // Promote service to foreground to prevent system termination
        startForeground(NOTIFICATION_ID, createNotification());
        
        // Start monitoring loops
        startMonitoring();
        startProximityMonitoring();
        
        Log.d(TAG, "AppBlockingService started");
    }

    /**
     * Configures the required notification channel for the service on Android 8.0+.
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Blocking Service",
                    NotificationManager.IMPORTANCE_LOW // Low priority to avoid being intrusive
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                // Register notification  channel with the system
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    /**
     * Creates the persistent notification displayed while the service is running.
     * 
     * @return A configured Notification object.
     */
    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blocking apps")
                .setContentText("Monitoring for focused apps during lecture")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    /**
     * Called by the system every time a client explicitly starts the service.
     *
     * @return START_STICKY to tell the system to recreate the service if it is killed.
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Binding is not supported for this service.
     * @return null always.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    /**
     * Initiates the recursive loop for monitoring the foreground application.
     */
    private void startMonitoring() {
        checkAppRunnable = new Runnable() {
            @Override
            public void run() {
                // First check if the service should still be active
                if (shouldStopBlocking()) {
                    // Self-terminate if lecture ended
                    stopSelf();
                    return;
                }
                // Perform app check
                checkForegroundApp();
                // Schedule next check
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        // Trigger initial execution
        handler.post(checkAppRunnable);
    }

    /**
     * Initiates the recursive loop for monitoring physical proximity.
     */
    private void startProximityMonitoring() {
        proximityCheckRunnable = new Runnable() {
            @Override
            public void run() {
                // Perform location check
                checkProximity();
                // Schedule next check (every 30s)
                handler.postDelayed(this, PROXIMITY_CHECK_INTERVAL);
            }
        };
        // Trigger initial execution
        handler.post(proximityCheckRunnable);
    }

    /**
     * Checks if the current lecture session has expired based on system time.
     * 
     * @return true if the lecture has ended and blocking should stop.
     */
    private boolean shouldStopBlocking() {
        SharedPreferences prefs = getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE);
        // Get end time stored during verification
        long endTime = prefs.getLong(VerificationCodeFragment.KEY_LECTURE_END_TIME, 0);
        
        // Compare with current system time
        if (System.currentTimeMillis() > endTime) {
            Log.d(TAG, "Stopping service: Lecture ended");
            // Perform preference cleanup
            disableBlocking();
            return true;
        }
        
        return false;
    }

    /**
     * Triggers a location check to see if the user is still near the lecture building.
     * If they have left the 50m radius, the service self-terminates.
     */
    private void checkProximity() {
        SharedPreferences prefs = getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE);
        // Retrieve verified lecture location data
        String building = prefs.getString(VerificationCodeFragment.KEY_LECTURE_BUILDING, "");
        String room = prefs.getString(VerificationCodeFragment.KEY_LECTURE_ROOM, "");

        // Only check if we have a valid building target
        if (building.isEmpty()) return;

        // Perform async check with 50m threshold
        proximityChecker.check(building, room, 50.0, null, isNearby -> {
            // Callback logic on proximity result
            if (!isNearby) {
                Log.d(TAG, "Stopping service: User moved outside 50m proximity");
                // Cleanup and stop
                disableBlocking();
                stopSelf();
            }
        });
    }

    /**
     * Clears blocking-related preferences to ensure consistency when the service stops.
     */
    private void disableBlocking() {
        // Disable general blocking toggle
        getSharedPreferences(AppBlockingManager.PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(AppBlockingManager.KEY_BLOCKING_ENABLED, false)
                .apply();
        
        // Reset attendance verification status
        getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(VerificationCodeFragment.KEY_CODE_VERIFIED, false)
                .apply();
    }

    /**
     * Logic to identify the active app and determine if it should be blocked.
     */
    private void checkForegroundApp() {
        // Double check that blocking is actually enabled
        if (!blockingManager.isBlockingEnabled()) {
            return;
        }

        // Use UsageStatsManager to find the current package in the foreground
        String foregroundApp = getForegroundApp();
        
        // Ignore if foreground app is this app itself
        if (foregroundApp != null && !foregroundApp.equals(ownPackageName)) {
            if (blockingManager.isAppBlocked(foregroundApp)) {
                Log.i(TAG, "BLOCKING: " + foregroundApp);
                launchBlockingActivity();
            }
        }
    }

    /**
     * Queries the system for the package name of the app currently in focus.
     * 
     * @return Package name string or null if unable to determine.
     */
    private String getForegroundApp() {
        // Access UsageStats system service
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return null;

        long endTime = System.currentTimeMillis();
        // Look at events in the last 2 seconds
        long startTime = endTime - 2000;

        // Query system usage events
        UsageEvents events = usm.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();
        String lastPackage = null;

        // Iterate through events to find the most recent MOVE_TO_FOREGROUND transition
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.getPackageName();
            }
        }
        return lastPackage;
    }

    /**
     * Launches the full-screen BlockingActivity to intercept user interaction.
     */
    private void launchBlockingActivity() {
        Intent intent = new Intent(this, BlockingActivity.class);
        // Flags to ensure it appears as a new task and replaces any existing instance
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT 
                | Intent.FLAG_ACTIVITY_SINGLE_TOP 
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    /**
     * Cleans up resources and stops monitoring loops when the service is destroyed.
     */
    @Override
    public void onDestroy() {
        // Super cleanup
        super.onDestroy();
        // Remove all pending callbacks from the handler
        if (handler != null) {
            handler.removeCallbacks(checkAppRunnable);
            handler.removeCallbacks(proximityCheckRunnable);
        }
    }
}
