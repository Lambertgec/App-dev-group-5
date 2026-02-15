package com.group5.gue;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CalendarHandler extends AppCompatActivity {
    Cursor cursor;

    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    public ArrayList<String> getCalendars(ContentResolver cr){
        Cursor cursor = null;
        Uri uri = CalendarContract.Calendars.CONTENT_URI;

        try {
            cursor = cr.query(uri, EVENT_PROJECTION, null, null, null);
        } catch (Exception e) {
            Log.d("calendar", Log.getStackTraceString(e));
        }

        ArrayList<String> calendarList = new ArrayList<>();
        while (cursor.moveToNext()) {
            long calID = 0;
            String displayName = null;

            calID = cursor.getLong(PROJECTION_ID_INDEX);
            displayName = cursor.getString(PROJECTION_DISPLAY_NAME_INDEX);

            Log.d("calendar", "fetched " + displayName );
            calendarList.add(displayName);

        }
        return calendarList;
    }

}
