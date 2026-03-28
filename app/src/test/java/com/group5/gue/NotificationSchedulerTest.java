package com.group5.gue;

import static com.group5.gue.notifications.NotificationScheduler.scheduleCatchUp;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.group5.gue.notifications.NotificationReceiver;
import com.group5.gue.notifications.NotificationSoonReceiver;

import static org.mockito.Mockito.mock;
import static org.robolectric.Shadows.shadowOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import androidx.test.core.app.ApplicationProvider;

import java.util.List;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class NotificationSchedulerTest {

    private Context context;
    private Event event;

    @Before
    public void setup() {
        context = ApplicationProvider.getApplicationContext();
        event = mock(Event.class);
        event.title = "Calculus";
        when(event.getLocation()).thenReturn("Auditorium 2");
    }

    @Test
    public void scheduleCatchUp_beforeWindow_doesNothing() {
        // Event is 2 hours away — outside the 30 min window
        when(event.getStartTime()).thenReturn(System.currentTimeMillis() + (120 * 60 * 1000));

        // Should fire nothing — no broadcast sent
        scheduleCatchUp(context, event);

        List<Intent> broadcasts = shadowOf((Application) context).getBroadcastIntents();
        assertTrue(broadcasts.isEmpty());
    }

    @Test
    public void scheduleCatchUp_inThirtyMinWindow_firesStandardNotification() {
        // Event is 20 mins away — inside the [−30, −10) window
        when(event.getStartTime()).thenReturn(System.currentTimeMillis() + (20 * 60 * 1000));

        scheduleCatchUp(context, event);

        List<Intent> broadcasts = shadowOf((Application) context).getBroadcastIntents();
        assertEquals(1, broadcasts.size());
        assertEquals(NotificationReceiver.class.getName(),
                broadcasts.get(0).getComponent().getClassName());
        assertEquals("30 minutes", broadcasts.get(0).getStringExtra("label"));
    }

    @Test
    public void scheduleCatchUp_inTenMinWindow_firesProximityNotification() {
        // Event is 5 mins away — inside the [−10, start) window
        when(event.getStartTime()).thenReturn(System.currentTimeMillis() + (5 * 60 * 1000));

        scheduleCatchUp(context, event);

        List<Intent> broadcasts = shadowOf((Application) context).getBroadcastIntents();
        assertEquals(1, broadcasts.size());
        assertEquals(NotificationSoonReceiver.class.getName(),
                broadcasts.get(0).getComponent().getClassName());
        assertNull(broadcasts.get(0).getStringExtra("label"));
    }

    @Test
    public void scheduleCatchUp_afterEventStart_doesNothing() {
        // Event started 5 mins ago
        when(event.getStartTime()).thenReturn(System.currentTimeMillis() - (5 * 60 * 1000));

        scheduleCatchUp(context, event);

        List<Intent> broadcasts = shadowOf((Application) context).getBroadcastIntents();
        assertTrue(broadcasts.isEmpty());
    }
}