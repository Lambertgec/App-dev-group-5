package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;

@RunWith(RobolectricTestRunner.class)
public class EventBarManagerTest {

    private EventBarManager eventBarManager;
    private TextView eventBar;
    private CalendarHandler calendarHandler;
    private String lastClickedLocation;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        eventBar = new TextView(context);
        calendarHandler = mock(CalendarHandler.class);
        eventBarManager = new EventBarManager(eventBar, calendarHandler, location -> lastClickedLocation = location);
    }

    @Test
    public void testRefresh_NoPermission() {
        EventBarManager managerNoHandler = new EventBarManager(eventBar, null, loc -> {});
        managerNoHandler.refresh(ApplicationProvider.getApplicationContext());
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.calendar_permission_message), eventBar.getText().toString());
    }

    @Test
    public void testRefresh_OngoingEvent() {
        ArrayList<Event> ongoing = new ArrayList<>();
        Event e = mock(Event.class);
        when(e.getLocation()).thenReturn("Atlas 1.100");
        ongoing.add(e);
        when(calendarHandler.getOngoingEvent()).thenReturn(ongoing);

        eventBarManager.refresh(ApplicationProvider.getApplicationContext());
        String expected = ApplicationProvider.getApplicationContext().getString(R.string.event_happening) + " Atlas 1.100";
        assertEquals(expected, eventBar.getText().toString());
    }

    @Test
    public void testRefresh_UpcomingEvent() {
        when(calendarHandler.getOngoingEvent()).thenReturn(new ArrayList<>());
        ArrayList<Event> upcoming = new ArrayList<>();
        Event e = mock(Event.class);
        when(e.getLocation()).thenReturn("Flux 1.01");
        upcoming.add(e);
        when(calendarHandler.getStartingSoon()).thenReturn(upcoming);

        eventBarManager.refresh(ApplicationProvider.getApplicationContext());
        String expected = ApplicationProvider.getApplicationContext().getString(R.string.event_starting_soon) + " Flux 1.01";
        assertEquals(expected, eventBar.getText().toString());
    }

    @Test
    public void testRefresh_NoEvents() {
        when(calendarHandler.getOngoingEvent()).thenReturn(new ArrayList<>());
        when(calendarHandler.getStartingSoon()).thenReturn(new ArrayList<>());

        eventBarManager.refresh(ApplicationProvider.getApplicationContext());
        assertEquals(ApplicationProvider.getApplicationContext().getString(R.string.no_event_soon), eventBar.getText().toString());
    }
}
