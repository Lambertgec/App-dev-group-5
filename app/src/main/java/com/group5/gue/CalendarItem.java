package com.group5.gue;

public class CalendarItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_EVENT = 1;

    public final int type;
    public final String header;   // non-null if TYPE_HEADER
    public final Event event;     // non-null if TYPE_EVENT

    public CalendarItem(String header) {
        this.type = TYPE_HEADER;
        this.header = header;
        this.event = null;
    }

    public CalendarItem(Event event) {
        this.type = TYPE_EVENT;
        this.event = event;
        this.header = null;
    }
}
