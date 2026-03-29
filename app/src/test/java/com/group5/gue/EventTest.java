package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class EventTest {

    @Test
    public void testEventFields() {
        Event event = new Event();
        event.title = "Math Lecture";
        event.startTime = 1000L;
        event.endTime = 2000L;
        event.location = "Auditorium 1";

        assertEquals("Math Lecture", event.title);
        assertEquals(1000L, event.startTime);
        assertEquals(2000L, event.endTime);
        assertEquals("Auditorium 1", event.location);
    }

    @Test
    public void testGetters() {
        Event event = new Event();
        event.location = "MetaForum";
        event.startTime = 5000L;
        event.endTime = 6000L;

        assertEquals("MetaForum", event.getLocation());
        assertEquals(5000L, event.getStartTime());
        assertEquals(6000L, event.getEndTime());
    }

    @Test
    public void testToString() {
        Event event = new Event();
        event.title = "Test Event";
        event.location = "Room A";
        
        String toString = event.toString();
        assertTrue(toString.contains("Test Event"));
        assertTrue(toString.contains("Room A"));
    }
}
