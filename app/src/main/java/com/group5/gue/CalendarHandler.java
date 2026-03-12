package com.group5.gue;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class CalendarHandler {
    private Cursor cursor;

    private String calendarName;
    private final ContentResolver contentResolver;

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

    public ArrayList<String> fetchEvents() {

        ArrayList<String> eventList = new ArrayList<>();
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

                    String eventTitle = cursor.getString(1);
                    Long eventStart = cursor.getLong(2);
                    Long eventEnd = cursor.getLong(3);

    //                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm");
                    DateFormat formatter = SimpleDateFormat.getDateTimeInstance();

                    eventList.add(eventTitle + " " + formatter.format(eventStart) + " " + formatter.format(eventEnd) + "\n\n");
                }
            } catch (Exception e) {
                Log.d("calendar", Log.getStackTraceString(e));
            }
        }
        return eventList;
    }

    public void setCalendar(String calendarName) {
        this.calendarName = calendarName;
    }
}
