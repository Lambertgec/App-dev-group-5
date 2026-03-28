package com.group5.gue.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.group5.gue.MainActivity;

/**
 * BroadcastReceiver that displays a proximity reminder notification when a lecture
 * is 10 minutes away. Fired by {@link NotificationScheduler} and intended to prompt
 * the user to be physically near their lecture venue so they can submit their
 * attendance verification code.
 * Tapping the notification opens {@link MainActivity} and navigates to the home tab.
 */
public class NotificationSoonReceiver extends BroadcastReceiver {

    /**
     * Handles the incoming broadcast by validating the location extra and, if
     * present, delegating to {@link #showNotification(Context, String, String)}.
     *
     * @param context the Context in which the receiver is running
     * @param intent  the Intent that triggered this receiver; must carry
     *                {@code "title"} and a non-empty {@code "location"} extra
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        String location = intent.getStringExtra("location");

        if (location == null || location.isEmpty()) return;

        showNotification(context, title, location);
    }

    /**
     * Constructs and posts a high-priority notification reminding the user that
     * their lecture starts in 10 minutes and that they will soon be able to enter
     * their attendance verification code.
     *
     * @param context  the Context used to build and post the notification
     * @param title    the lecture/event title shown in the notification body
     * @param location the room or building name (used for channel routing; the body
     *                 text references the event title rather than the raw location)
     */
    public void showNotification(Context context, String title, String location) {
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "proximity_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Proximity Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.putExtra("open_tab", "home");
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Lecture starts in 10 minutes!")
                        .setContentText("You can input your verification code for " + title +
                                " in 10 minutes.")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}