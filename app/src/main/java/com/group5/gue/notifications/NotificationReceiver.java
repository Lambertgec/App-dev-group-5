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
 * BroadcastReceiver that displays a reminder notification when a scheduled lecture
 * is approaching. Fired by {@link NotificationScheduler} 30 minutes before a lecture
 * starts, or immediately via {@code scheduleCatchUp} if the app is opened within
 * that window. Tapping the notification opens {@link MainActivity} and navigates to the map tab.
 */
public class NotificationReceiver extends BroadcastReceiver {

    /**
     * Handles the incoming broadcast by constructing and posting a high-priority
     * lecture reminder notification. Creates the {@code "lecture_channel"} notification channel.
     * The notification is automatically dismissed when tapped.
     *
     * @param context the Context in which the receiver is running
     * @param intent  the Intent that triggered this receiver; must carry
     *                {@code "title"} and {@code "location"} extras, and optionally
     *                a {@code "label"} extra to override the default time text
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        String title = intent.getStringExtra("title");
        String location = intent.getStringExtra("location");

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String channelId = "lecture_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Lecture Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.putExtra("open_tab", "map");
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String label = intent.getStringExtra("label");
        String reminderText = label != null ? label : "30 minutes";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Lecture starting in " + reminderText + "!")
                .setContentText(title + " at " + location)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);              // closes notification when clicked

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}