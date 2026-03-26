package com.group5.gue;

public class Event {
    public String title;
    public long startTime;
    public long endTime;
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

    public String getLocation() {
        return location;
    }

    public long getStartTime() {
        return startTime;
    }
}