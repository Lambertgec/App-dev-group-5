package com.group5.gue.data.model;

/**
 * Represents a record of a user's attendance at an event.
 * This class stores details about the event, location, and the time of attendance.
 */
public class AttendanceRecord {
    // The name of the event attended.
    private String eventName;
    // The building where the event took place.
    private String building;
    // The specific room name or number within the building.
    private String roomName;
    // The timestamp representing when the attendance was recorded.
    private long timestamp;

    /**
     * Constructs a new AttendanceRecord with the specified details.
     *
     * @param eventName The name of the event.
     * @param building The building of the event.
     * @param roomName The room of the event.
     * @param timestamp The time of attendance.
     */
    public AttendanceRecord(String eventName, String building, String roomName, long timestamp) {
        this.eventName = eventName;
        this.building = building;
        this.roomName = roomName;
        this.timestamp = timestamp;
    }

    // @return The name of the event.
    public String getEventName() { return eventName; }
    // @return The building name.
    public String getBuilding() { return building; }
    // @return The room name or identifier.
    public String getRoomName() { return roomName; }
    // @return The recording timestamp.
    public long getTimestamp() { return timestamp; }
}