package com.group5.gue.data.model;

public class AttendanceRecord {
    private String eventName;
    private String building;
    private String roomName;
    private long timestamp;

    public AttendanceRecord(String eventName, String building, String roomName, long timestamp) {
        this.eventName = eventName;
        this.building = building;
        this.roomName = roomName;
        this.timestamp = timestamp;
    }

    public String getEventName() { return eventName; }
    public String getBuilding() { return building; }
    public String getRoomName() { return roomName; }
    public long getTimestamp() { return timestamp; }
}