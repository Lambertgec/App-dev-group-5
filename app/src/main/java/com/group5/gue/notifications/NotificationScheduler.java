package com.group5.gue.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.group5.gue.Event;

public class NotificationScheduler {

    public static void scheduleNotification(Context context, Event event) {

        //long triggerTime = System.currentTimeMillis() + 15000; // 15 seconds for easy check
        long triggerTime = event.getStartTime() - (30 * 60 * 1000);

        if (triggerTime < System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("title", event.title);
        intent.putExtra("location", event.getLocation());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                event.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {

                Intent settingsIntent =
                        new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);

                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);

                return;
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );
    }

    public static void scheduleProximityNotification(Context context, Event event) {
        long triggerTime = event.getStartTime() - (10 * 60 * 1000); // 10 minutes before

        if (triggerTime < System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, ProximityNotificationReceiver.class);
        intent.putExtra("title", event.title);
        intent.putExtra("location", event.getLocation());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                ("proximity_" + event.hashCode()).hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        AlarmManager alarmManager =
                (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Intent settingsIntent =
                        new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                settingsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(settingsIntent);
                return;
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
        );
    }

    public static void scheduleCatchUp(Context context, Event event) {
        long now = System.currentTimeMillis();
        long thirtyMinMark = event.getStartTime() - (30 * 60 * 1000);
        long tenMinMark = event.getStartTime() - (10 * 60 * 1000);

        // If we are in the window between 30 and 10 mins before, fire the 30-min notification
        if (now >= thirtyMinMark && now < tenMinMark) {
            fireImmediately(context, event, NotificationReceiver.class, "30 minutes");
        }
        // If we are in the window 10 mins before start, fire the proximity/verification notification
        else if (now >= tenMinMark && now < event.getStartTime()) {
            fireImmediately(context, event, ProximityNotificationReceiver.class, null);
        }
    }

    private static void fireImmediately(Context context, Event event, Class<?> receiverClass, String minuteLabel) {
        Intent intent = new Intent(context, receiverClass);
        intent.putExtra("title", event.title);
        intent.putExtra("location", event.getLocation());
        if (minuteLabel != null) {
            intent.putExtra("label", minuteLabel);
        }
        context.sendBroadcast(intent);
    }
}
