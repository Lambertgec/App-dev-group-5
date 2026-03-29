package com.group5.gue.data;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Utility class for managing application permissions and system settings access.
 * This class encapsulates the logic for checking and requesting both standard 
 * runtime permissions and specialized system-level permissions required for app blocking.
 *
 */
public class PermissionHandler {

    /** The host activity used for permission requests and starting system settings intents. */
    Activity activity;

    /**
     * Constructs a PermissionHandler with the given activity context.
     *
     * @param activity The activity from which to request permissions.
     */
    public PermissionHandler(Activity activity) {
        this.activity = activity;
    }

    /**
     * Checks if a specific runtime permission is currently granted.
     *
     * @param permission The permission identifier (e.g., Manifest.permission.READ_CALENDAR).
     * @return true if granted, false otherwise.
     */
    public boolean checkPermission(String permission) {
        return ContextCompat.checkSelfPermission(activity,
                permission)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Triggers a standard system request dialog for a single runtime permission.
     *
     * @param permission The permission string to request.
     */
    public void requestPermission(String permission) {
        // Execute the system request dialog
        ActivityCompat.requestPermissions(this.activity,
                new String[]{permission},
                1);
    }

    /**
     * Coordinates the sequence of requests needed for the App Blocking feature.
     * Includes POST_NOTIFICATIONS (Android 13+), Usage Access, and Overlay permissions.
     */
    public void requestAppBlocking() {

        // Handle POST_NOTIFICATIONS for Android 13 (API 33) and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request notification permission if not yet granted
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
            }
        }

        // Verify if "Usage Stats" permission is active (required to detect foreground apps)
        if (!hasUsageStatsPermission()) {
            // Direct user to the specific system settings page for Usage Access
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            activity.startActivity(intent);
        }

        // Verify if "Draw Over Other Apps" permission is active (required for blocking overlay)
        if (!Settings.canDrawOverlays(activity)) {
            // Direct user to the system settings page for Overlay permissions
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivity(intent);
        }
    }

    /**
     * Determines if the application has been granted Usage Access by the user.
     * This uses AppOpsManager as Usage Access is not a standard runtime permission.
     *
     * @return true if access is allowed, false otherwise.
     */
    private boolean hasUsageStatsPermission() {
        // Access the AppOpsManager service
        AppOpsManager appOps = (AppOpsManager) activity.getSystemService(Context.APP_OPS_SERVICE);
        // Check the operation status for usage stats
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), activity.getPackageName());
        // Return true if specifically allowed
        return mode == AppOpsManager.MODE_ALLOWED;
    }
}
