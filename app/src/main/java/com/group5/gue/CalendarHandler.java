package com.group5.gue;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Utility class for interacting with the Android Calendar Provider.
 * This class provides methods to retrieve calendars and events based on various criteria.
 */
public class CalendarHandler {
    private Cursor cursor;

    private String calendarName;
    private final ContentResolver contentResolver;
    /** The name of the currently selected calendar. */
    public static String selectedCalendar = null;

    /**
     * Constructs a CalendarHandler using an Activity context.
     *
     * @param activity The activity from which to get the ContentResolver.
     */
    public CalendarHandler(Activity activity) {
        this.contentResolver = activity.getContentResolver();
    }

    /**
     * Constructs a CalendarHandler using a ContentResolver.
     *
     * @param contentResolver The ContentResolver to use for queries.
     */
    public CalendarHandler(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    /**
     * Retrieves a list of all available calendar display names.
     *
     * @return A list of strings containing calendar display names.
     */
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
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String displayName = cursor.getString(1);
                calendarList.add(displayName);
            }
            cursor.close();
        }

        return calendarList;
    }

    /**
     * Retrieves all events from the currently set calendar.
     *
     * @return A list of Event objects.
     */
    public ArrayList<Event> getAllEvents() {
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?";

        String[] selectionArgs = new String[] {
                this.calendarName};

        return submitQuery(selection, selectionArgs);
    }


    /**
     * Retrieves events that are currently ongoing.
     *
     * @return A list of Event objects that started before now and end after now.
     */
    public ArrayList<Event> getOngoingEvent() {
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ? AND " +
                CalendarContract.Events.DTSTART + " <= ? AND " +
                CalendarContract.Events.DTEND + " >= ?";

        String[] selectionArgs = new String[]{
                this.calendarName,
                String.valueOf(System.currentTimeMillis()),
                String.valueOf(System.currentTimeMillis())};

        return submitQuery(selection, selectionArgs);
    }

    /**
     * Retrieves events starting soon.
     *
     * @return A list of Event objects starting within the next hour.
     */
    public ArrayList<Event> getStartingSoon() {
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ? AND " +
                        CalendarContract.Events.DTSTART + " >= ? AND " +
                        CalendarContract.Events.DTSTART + " <= ?";

        Long timeNow = System.currentTimeMillis();

        String[] selectionArgs = new String[]{
                this.calendarName,
                String.valueOf(timeNow),
                String.valueOf(timeNow + 3600000)};

        return submitQuery(selection, selectionArgs);
    }


    /**
     * Retrieves events for a specific day.
     *
     * @param startOfDay The start of the day in milliseconds.
     * @return A list of Event objects occurring on that day.
     */
    public ArrayList<Event> getDay(Long startOfDay) {
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ? AND " +
                        CalendarContract.Events.DTSTART + " >= ? AND " +
                        CalendarContract.Events.DTSTART + " < ?";

        String[] selectionArgs = new String[]{
                this.calendarName,
                String.valueOf(startOfDay),
                String.valueOf(startOfDay + 86400000)};

        return submitQuery(selection, selectionArgs);
    }

    /**
     * Helper method to execute a query against the Calendar Provider.
     *
     * @param query The selection string.
     * @param args The selection arguments.
     * @return A list of Event objects matching the query.
     */
    private ArrayList<Event> submitQuery(String query, String[] args) {

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

            try {
                cursor = contentResolver.query(uri, EVENT_PROJECTION, query, args, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        Event event = new Event();

                        event.title = cursor.getString(1);
                        event.startTime = cursor.getLong(2);
                        event.endTime = cursor.getLong(3);
                        event.location = cursor.getString(4);

                        eventList.add(event);
                    }
                    cursor.close();
                }
            } catch (Exception e) {
                Log.d("calendar", Log.getStackTraceString(e));
            }
        }
        return eventList;
    }

    /**
     * Retrieves all events scheduled to start in the future.
     *
     * @return A list of future Event objects.
     */
    public ArrayList<Event> getFutureEvents() {
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ? AND " +
                        CalendarContract.Events.DTSTART + " >= ?";

        String[] selectionArgs = new String[]{
                this.calendarName,
                String.valueOf(System.currentTimeMillis())
        };

        return submitQuery(selection, selectionArgs);
    }

    /**
     * Sets the calendar to be used for future queries.
     *
     * @param calendarName The name of the calendar.
     */
    void setCalendar(String calendarName) {
        this.calendarName = calendarName;
    }

}
