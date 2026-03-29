package com.group5.gue;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import java.util.ArrayList;

/**
 * Utility class for interacting with the Android Calendar Provider.
 * This class provides methods to retrieve calendars and events based on various criteria.
 * It encapsulates the complexities of querying the CalendarContract.
 *
 */
public class CalendarHandler {
    /** Temporary cursor for query results. */
    private Cursor cursor;

    /** Name of the calendar to query events from. */
    private String calendarName;

    /** ContentResolver used to access the calendar data. */
    private final ContentResolver contentResolver;
    /** The name of the currently selected calendar. */
    public static String selectedCalendar = null;

    /**
     * Constructs a CalendarHandler using an Activity context.
     * Extracts the ContentResolver from the provided activity.
     *
     * @param activity The activity from which to get the ContentResolver.
     */
    public CalendarHandler(Activity activity) {
        this.contentResolver = activity.getContentResolver();
    }

    /**
     * Constructs a CalendarHandler using a ContentResolver directly.
     * Useful for background services or workers where an Activity is not available.
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
        // Define the URI for calendar content
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        // Specify which columns to retrieve
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
        // Iterate through the results
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
     * @return A list of Event objects associated with the selected calendar.
     */
    public ArrayList<Event> getAllEvents() {
        // Selection criteria: display name must match the current calendarName
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ?";

        // Arguments for the selection
        String[] selectionArgs = new String[] {
                this.calendarName};

        // Submit the query with the defined criteria
        return submitQuery(selection, selectionArgs);
    }


    /**
     * Retrieves events that are currently ongoing based on system time.
     * An event is ongoing if start time <= now and end time >= now.
     *
     * @return A list of Event objects that started before now and end after now.
     */
    public ArrayList<Event> getOngoingEvent() {
        // Complex selection for ongoing events restricted by calendar name
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ? AND " +
                CalendarContract.Events.DTSTART + " <= ? AND " +
                CalendarContract.Events.DTEND + " >= ?";

        // Current time in milliseconds
        String now = String.valueOf(System.currentTimeMillis());
        String[] selectionArgs = new String[]{
                this.calendarName,
                now,
                now};

        return submitQuery(selection, selectionArgs);
    }

    /**
     * Retrieves events starting within the next hour from the current time.
     *
     * @return A list of Event objects starting soon.
     */
    public ArrayList<Event> getStartingSoon() {
        // Selection for events starting between now and one hour from now
        String selection =
                CalendarContract.Events.CALENDAR_DISPLAY_NAME + " = ? AND " +
                        CalendarContract.Events.DTSTART + " >= ? AND " +
                        CalendarContract.Events.DTSTART + " <= ?";

        // Current system time
        Long timeNow = System.currentTimeMillis();

        String[] selectionArgs = new String[]{
                this.calendarName,
                String.valueOf(timeNow),
                // 3,600,000 milliseconds = 1 hour
                String.valueOf(timeNow + 3600000)};

        // Fetch events starting soon
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
                // 86,400,000 milliseconds = 24 hours
                String.valueOf(startOfDay + 86400000)};

        return submitQuery(selection, selectionArgs);
    }

    /**
     * Core helper method to execute a query against the Calendar Provider.
     * Maps cursor rows to Event objects.
     *
     * @param query The selection string for the SQL WHERE clause.
     * @param args The arguments to replace placeholders in the selection string.
     * @return A list of Event objects matching the provided query parameters.
     */
    private ArrayList<Event> submitQuery(String query, String[] args) {
        ArrayList<Event> eventList = new ArrayList<>();

        // Only proceed if a calendar name is set
        if (calendarName != null) {
            Uri uri = CalendarContract.Events.CONTENT_URI;

            // Define which event columns to fetch
            String[] EVENT_PROJECTION = new String[]{
                    CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                    CalendarContract.Events.TITLE,
                    CalendarContract.Events.DTSTART,
                    CalendarContract.Events.DTEND,
                    CalendarContract.Events.EVENT_LOCATION
            };

            try {
                // Execute query using the contentResolver
                cursor = contentResolver.query(uri, EVENT_PROJECTION, query, args, null);

                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        // Create a new domain object for each row
                        Event event = new Event();

                        // Map columns to fields
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
