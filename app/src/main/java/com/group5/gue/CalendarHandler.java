package com.group5.gue;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;

public class CalendarHandler {
    private Cursor cursor;

    private String calendarName;
    private final ContentResolver contentResolver;
    public static String selectedCalendar = null;

    public CalendarHandler(Activity activity) {
        this.contentResolver = activity.getContentResolver();
    }

    public ArrayList<String> getCalendars(){
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        String[] EVENT_PROJECTION = new String[] {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        };

        try {
            cursor = contentResolver.query(uri, EVENT_PROJECTION, null, null, null);
        } catch (Exception e) {
            Log.d("calendar", Log.getStackTraceString(e));
        }

        ArrayList<String> calendarList = new ArrayList<>();
        while (cursor.moveToNext()) {
            long calID = 0;
            String displayName = null;

            calID = cursor.getLong(0);
            displayName = cursor.getString(1);

            calendarList.add(displayName);
        }

        return calendarList;
    }

    public ArrayList<Event> fetchEvents() {

        ArrayList<Event> eventList = new ArrayList<>();
        if (calendarName != null) {
            Uri uri = CalendarContract.Events.CONTENT_URI;

            String[] EVENT_PROJECTION = new String[] {
                    CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND
            };

            String selection = "((" + CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ?))";
            String[] selectionArgs = new String[] {this.calendarName};

            try {
                cursor = contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

                while (cursor.moveToNext()) {
                    Event event = new Event();

                    event.title = cursor.getString(1);
                    event.startTime = cursor.getLong(2);
                    event.endTime = cursor.getLong(3);

                    //                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
                    DateFormat formatter = SimpleDateFormat.getDateTimeInstance();

                    eventList.add(event);
                }
            } catch (Exception e) {
                Log.d("calendar", Log.getStackTraceString(e));
            }
        }
        return eventList;
    }


    public ArrayList<Event> getOngoingEvent() {

        ArrayList<Event> eventList = new ArrayList<>();
        if (calendarName != null) {
            Uri uri = CalendarContract.Events.CONTENT_URI;

            String[] EVENT_PROJECTION = new String[]{
                    CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.EVENT_LOCATION
            };

            String selection = "((" +
                    CalendarContract.Calendars.CALENDAR_DISPLAY_NAME + " = ? AND " +
                    CalendarContract.Events.DTSTART + " <= ? AND " +
                    CalendarContract.Events.DTEND + " >= ? ))";

            String[] selectionArgs = new String[]{
                    this.calendarName,
                    String.valueOf(System.currentTimeMillis()),
                    String.valueOf(System.currentTimeMillis())};

            try {
                cursor = contentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

                while (cursor.moveToNext()) {
                    Event event = new Event();

                    event.title = cursor.getString(1);
                    event.startTime = cursor.getLong(2);
                    event.endTime = cursor.getLong(3);
                    event.location = cursor.getString(4);

                    eventList.add(event);
                }
            } catch (Exception e) {
                Log.d("calendar", Log.getStackTraceString(e));
            }
        }
        return eventList;
    }

    void setCalendar(String calendarName) {
        this.calendarName = calendarName;
    }

}

class Event {
    String title;
    long startTime;
    long endTime;
    String location;

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
