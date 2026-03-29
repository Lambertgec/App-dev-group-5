package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

public class CalendarItemTest {

    @Test
    public void testHeaderConstructor() {
        String headerText = "Monday, May 1";
        CalendarItem item = new CalendarItem(headerText);
        
        assertEquals("Type should be HEADER", CalendarItem.TYPE_HEADER, item.type);
        assertEquals("Header text should match", headerText, item.header);
        assertNull("Event should be null for header type", item.event);
    }

    @Test
    public void testEventConstructor() {
        Event mockEvent = new Event();
        mockEvent.title = "Lecture 1";
        
        CalendarItem item = new CalendarItem(mockEvent);
        
        assertEquals("Type should be EVENT", CalendarItem.TYPE_EVENT, item.type);
        assertEquals("Event object should match", mockEvent, item.event);
        assertNull("Header should be null for event type", item.header);
    }
}
