package com.group5.gue;

/**
 * Represents a calendar event with a title, start time, end time, and location.
 */
public class Event {
    /** The title of the event. */
    public String title;
    /** The start time of the event in milliseconds since epoch. */
    public long startTime;
    /** The end time of the event in milliseconds since epoch. */
    public long endTime;
    /** The location where the event takes place. */
    public String location;

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", location='" + location + '\'' +
                '}';
    }

    /**
     * Gets the location of the event.
     *
     * @return The event location.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Gets the start time of the event.
     *
     * @return The start time in milliseconds.
     */
    public long getStartTime() {
        return startTime;
    }

    /**
     * Gets the end time of the event.
     *
     * @return The end time in milliseconds.
     */
    public long getEndTime() {
        return endTime;
    }
}