package com.group5.gue.notifications;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.group5.gue.Event;

/**
 * Utility class for scheduling and immediately firing event-related notifications.
 *
 * <p>There are two scheduled notification types, both using
 * {@link AlarmManager#setExactAndAllowWhileIdle} so they fire even when the device
 * is in low-power idle mode:
 * <ul>
 *   <li><b>Standard reminder</b> – fires 30 minutes before an event via
 *       {@link NotificationReceiver}.</li>
 *   <li><b>Proximity reminder</b> – fires 10 minutes before an event via
 *       {@link NotificationSoonReceiver}, prompting the user to be near
 *       their venue to submit an attendance verification code.</li>
 * </ul>
 *
 * <p>On Android 12 (API 31) and above, exact alarms require the
 * {@code SCHEDULE_EXACT_ALARM} permission. If that permission has not been
 * granted, the scheduling methods redirect the user to the system settings
 * screen and return without scheduling.
 *
 * <p>{@link #scheduleCatchUp(Context, Event)} handles the edge case where the
 * app is opened after a notification would already have fired, broadcasting the
 * appropriate notification immediately instead.
 */
public class NotificationScheduler {

    /**
     * Schedules a high-priority reminder notification to fire 30 minutes before
     * the given event starts.
     *
     * <p>If the calculated trigger time is already in the past the method returns
     * silently without scheduling anything.
     *
     * <p>The request code for the underlying {@link PendingIntent} is derived from
     * {@link Event#getStartTime()}, so re-scheduling the same event will replace
     * any previously registered alarm for it.
     *
     * @param context the application context used to obtain the {@link AlarmManager}
     *                and to construct the broadcast {@link Intent}
     * @param event   the event to schedule a reminder for; must have a valid start
     *                time, title, and location
     */
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
                (int) event.getStartTime(),
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

    /**
     * Schedules a proximity reminder notification to fire 10 minutes before the
     * given event starts, prompting the user to be near the venue so they can
     * submit their attendance verification code.
     *
     * <p>If the calculated trigger time is already in the past the method returns
     * silently without scheduling anything.
     *
     * <p>The request code for the underlying {@link PendingIntent} is derived from
     * a hash of {@code "proximity_"} prepended to {@link Event#hashCode()}, keeping
     * it distinct from the standard reminder's request code for the same event.
     *
     * @param context the application context used to obtain the {@link AlarmManager}
     *                and to construct the broadcast {@link Intent}
     * @param event   the event to schedule a proximity reminder for; must have a
     *                valid start time, title, and location
     */
    public static void scheduleProximityNotification(Context context, Event event) {
        long triggerTime = event.getStartTime() - (10 * 60 * 1000); // 10 minutes before

        if (triggerTime < System.currentTimeMillis()) {
            return;
        }

        Intent intent = new Intent(context, NotificationSoonReceiver.class);
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

    /**
     * Fires a missed notification immediately if the app is opened during a window
     * when a scheduled notification should already have been shown.
     *
     * <p>This compensates for alarms that may have been missed (e.g. the app was
     * force-stopped or the device was off). The two windows checked are:
     * <ul>
     *   <li><b>[−30 min, −10 min)</b> – broadcasts to {@link NotificationReceiver}
     *       with a label of {@code "30 minutes"}.</li>
     *   <li><b>[−10 min, start)</b> – broadcasts to
     *       {@link NotificationSoonReceiver} with no label override.</li>
     * </ul>
     * If the current time falls outside both windows (i.e. the event has already
     * started, or it is more than 30 minutes away) nothing is fired.
     *
     * @param context the application context used to send the broadcast
     * @param event   the event to check catch-up notifications for
     */
    public static void scheduleCatchUp(Context context, Event event) {
        long now = System.currentTimeMillis();
        long thirtyMinMark = event.getStartTime() - (30 * 60 * 1000);
        long tenMinMark = event.getStartTime() - (10 * 60 * 1000);

        // If we are in the window between 30 and 10 mins before, fire the 30-min notification
        if (now >= thirtyMinMark && now < tenMinMark) {
            fireImmediately(context, event, NotificationReceiver.class, "30 minutes");
        }
        // If we are in the window 10 mins before start, fire the proximity/verification
        // notification
        else if (now >= tenMinMark && now < event.getStartTime()) {
            fireImmediately(context, event, NotificationSoonReceiver.class, null);
        }
    }

    /**
     * Sends a broadcast immediately to the given receiver class, bypassing the
     * {@link AlarmManager}. Used by {@link #scheduleCatchUp(Context, Event)} to
     * deliver notifications that should have already fired.
     *
     * @param context       the application context used to send the broadcast
     * @param event         the event whose details are attached to the Intent
     * @param receiverClass the {@link android.content.BroadcastReceiver} class to target
     * @param minuteLabel   an optional time label (e.g. {@code "30 minutes"}) attached
     *                      as the {@code "label"} Intent extra; pass {@code null} to omit
     */
    private static void fireImmediately(Context context, Event event,
                                        Class<?> receiverClass, String minuteLabel) {
        Intent intent = new Intent(context, receiverClass);
        intent.putExtra("title", event.title);
        intent.putExtra("location", event.getLocation());
        if (minuteLabel != null) {
            intent.putExtra("label", minuteLabel);
        }
        context.sendBroadcast(intent);
    }
}