package com.group5.gue;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import org.junit.Before;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import com.group5.gue.notifications.NotificationSoonReceiver;

@RunWith(RobolectricTestRunner.class)
public class NotificationSoonReceiverTest {

    private Context context;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void onReceiveSoon_validLocation_postsNotification() {
        NotificationSoonReceiver receiver = new NotificationSoonReceiver();
        Intent intent = new Intent();
        intent.putExtra("title", "Math");
        intent.putExtra("location", "Room A");

        receiver.onReceive(context, intent);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        assertEquals(1, shadowOf(manager).getAllNotifications().size());
    }

    @Test
    public void onReceiveSoon_nullLocation_doesNothing() {
        NotificationSoonReceiver receiver = new NotificationSoonReceiver();
        Intent intent = new Intent();
        intent.putExtra("title", "Math");
        // no location extra

        receiver.onReceive(context, intent);

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        assertTrue(shadowOf(manager).getAllNotifications().isEmpty());
    }
}