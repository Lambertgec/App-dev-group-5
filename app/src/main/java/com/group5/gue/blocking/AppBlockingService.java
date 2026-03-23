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

import com.group5.gue.R;

public class AppBlockingService extends Service {
    private static final String TAG = "AppBlockingService";
    private static final String CHANNEL_ID = "AppBlockingChannel";
    private static final int NOTIFICATION_ID = 12345;
    private static final long CHECK_INTERVAL = 500;

    private static final String PREFS_LECTURE = "lecture_prefs";
    private static final String KEY_LECTURE_END_TIME = "lecture_end_time";
    private static final String KEY_ATTENDANCE_VERIFIED = "attendance_verified";

    private Handler handler;
    private Runnable checkAppRunnable;
    private String ownPackageName;
    private AppBlockingManager blockingManager;

    @Override
    public void onCreate() {
        super.onCreate();
        ownPackageName = getPackageName();
        blockingManager = new AppBlockingManager(this);
        handler = new Handler();
        
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, createNotification());
        
        startMonitoring();
        Log.d(TAG, "AppBlockingService started");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Blocking Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(serviceChannel);
        }
    }

    private Notification createNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blocking apps")
                .setContentText("persistent notification to keep service active")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    private void startMonitoring() {
        checkAppRunnable = new Runnable() {
            @Override
            public void run() {
                checkForegroundApp();
                handler.postDelayed(this, CHECK_INTERVAL);
            }
        };
        handler.post(checkAppRunnable);
    }

    private void checkForegroundApp() {
        if (!shouldBlock()) {
            return;
        }

        String foregroundApp = getForegroundApp();
        if (foregroundApp != null && !foregroundApp.equals(ownPackageName)) {
            if (blockingManager.isAppBlocked(foregroundApp)) {
                Log.i(TAG, "BLOCKING: " + foregroundApp);
                launchBlockingActivity();
            }
        }
    }

    private boolean shouldBlock() {
        SharedPreferences lecturePrefs = getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE);
        boolean isAttendanceVerified = lecturePrefs.getBoolean(KEY_ATTENDANCE_VERIFIED, false);
        long lectureEndTime = lecturePrefs.getLong(KEY_LECTURE_END_TIME, 0);

        if (isAttendanceVerified) {
//            in lecture AND user has blocking on
            if (System.currentTimeMillis() < lectureEndTime) {
                return blockingManager.isBlockingEnabled();
            } else {
                lecturePrefs.edit().putBoolean(KEY_ATTENDANCE_VERIFIED, false).apply();
            }
        }

        return false;
    }

    private String getForegroundApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        if (usm == null) return null;

        long endTime = System.currentTimeMillis();
        long startTime = endTime - 2000;

        UsageEvents events = usm.queryEvents(startTime, endTime);
        UsageEvents.Event event = new UsageEvents.Event();
        String lastPackage = null;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastPackage = event.getPackageName();
            }
        }
        return lastPackage;
    }

    private void launchBlockingActivity() {
        Intent intent = new Intent(this, BlockingActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
                | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT 
                | Intent.FLAG_ACTIVITY_SINGLE_TOP 
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) handler.removeCallbacks(checkAppRunnable);
    }
}
