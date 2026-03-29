package com.group5.gue;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.group5.gue.data.model.AttendanceRecord;

public class AttendanceRecordTest {

    @Test
    public void testConstructorAndGetters() {
        String event = "App Development";
        String building = "Auditorium";
        String room = "Room 1";
        long time = 123456789L;

        AttendanceRecord record = new AttendanceRecord(event, building, room, time);

        assertEquals("Event name should match", event, record.getEventName());
        assertEquals("Building name should match", building, record.getBuilding());
        assertEquals("Room name should match", room, record.getRoomName());
        assertEquals("Timestamp should match", time, record.getTimestamp());
    }
}
