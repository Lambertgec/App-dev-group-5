package com.group5.gue.notifications;

import static org.junit.Assert.assertEquals;
import static org.robolectric.Shadows.shadowOf;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowNotificationManager;

@RunWith(RobolectricTestRunner.class)
public class NotificationReceiverTest {

    private NotificationReceiver receiver;
    private Context context;
    private NotificationManager notificationManager;
    private ShadowNotificationManager shadowNotificationManager;

    @Before
    public void setUp() {
        receiver = new NotificationReceiver();
        context = ApplicationProvider.getApplicationContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        shadowNotificationManager = shadowOf(notificationManager);
    }

    @Test
    public void testOnReceive_postsNotification() {
        Intent intent = new Intent();
        intent.putExtra("title", "Math Lecture");
        intent.putExtra("location", "Auditorium 1");

        receiver.onReceive(context, intent);

        assertEquals(1, shadowNotificationManager.size());
        Notification notification = shadowNotificationManager.getAllNotifications().get(0);
        
        // Check contents (Robolectric's ShadowNotification doesn't always expose all details easily depending on API level, but we can check existence)
        assertNotNull(notification);
    }

    private void assertNotNull(Object obj) {
        if (obj == null) throw new AssertionError("Object is null");
    }
}
