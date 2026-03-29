package com.group5.gue;

/**
 * Wrapper class used for representing items in a scrollable calendar list.
 * A CalendarItem can either be a date header (e.g., "Monday, May 1") or 
 * a specific lecture/event entry.
 */
public class CalendarItem {
    // Item type constant for section headers.
    public static final int TYPE_HEADER = 0;
    // Item type constant for individual events.
    public static final int TYPE_EVENT = 1;

    // The type of this list item (HEADER or EVENT).
    public final int type;
    public final String header;   // non-null if TYPE_HEADER
    public final Event event;     // non-null if TYPE_EVENT

    /**
     * Constructs a header-type CalendarItem.
     * 
     * @param header The string to display as a list separator.
     */
    public CalendarItem(String header) {
        this.type = TYPE_HEADER;
        this.header = header;
        this.event = null;
    }

    /**
     * Constructs an event-type CalendarItem.
     * 
     * @param event The data for the specific lecture or meeting.
     */
    public CalendarItem(Event event) {
        this.type = TYPE_EVENT;
        this.event = event;
        this.header = null;
    }
}
