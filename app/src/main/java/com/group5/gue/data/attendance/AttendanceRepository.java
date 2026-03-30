package com.group5.gue.data.attendance;

import android.content.Context;
import android.content.SharedPreferences;

import com.group5.gue.data.model.AttendanceRecord;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Repository for managing attendance records locally using SharedPreferences.
 * This class handles saving, retrieving, and checking for duplicate attendance entries.
 */
public class AttendanceRepository {

    // Name of the SharedPreferences file.
    private static final String PREFS_NAME = "attendance_prefs";
    // Key used for storing the JSON array of attendance records.
    private static final String KEY_RECORDS = "attendance_records";

    // Singleton instance of the repository.
    private static AttendanceRepository instance;
    // SharedPreferences instance for local persistence.
    private final SharedPreferences prefs;

    /**
     * Private constructor to enforce Singleton pattern.
     * @param context The application context.
     */
    private AttendanceRepository(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Returns the singleton instance of AttendanceRepository.
     *
     * @param context The context used to initialize the repository if needed.
     * @return The singleton instance.
     */
    public static AttendanceRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (AttendanceRepository.class) {
                if (instance == null) {
                    instance = new AttendanceRepository(context);
                }
            }
        }
        return instance;
    }

    /**
     * Saves an attendance record locally, only if the same event has not
     * already been recorded today.
     *
     * @param record the attendance record to save
     * @return {@code true} if the record was saved, {@code false} if a record
     *         for the same event already exists for today
     */
    public boolean saveIfNotDuplicate(AttendanceRecord record) {
        List<AttendanceRecord> existing = getAll();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(record.getTimestamp()));

        // Check each existing record to see if it matches the event name and date.
        for (AttendanceRecord r : existing) {
            String recordDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date(r.getTimestamp()));
            if (r.getEventName().equals(record.getEventName()) && recordDate.equals(today)) {
                return false; // duplicate
            }
        }

        existing.add(record);
        saveAll(existing);
        return true;
    }

    /**
     * Returns all stored attendance records by parsing the JSON string in SharedPreferences.
     *
     * @return A list of all recorded attendance entries.
     */
    public List<AttendanceRecord> getAll() {
        List<AttendanceRecord> records = new ArrayList<>();
        String json = prefs.getString(KEY_RECORDS, "[]");

        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                records.add(new AttendanceRecord(
                        obj.getString("eventName"),
                        obj.getString("building"),
                        obj.getString("roomName"),
                        obj.getLong("timestamp")
                ));
            }
        } catch (JSONException e) {
            // Log error if JSON parsing fails.
            e.printStackTrace();
        }

        return records;
    }

    /**
     * Serializes the list of records into a JSON string and saves it to SharedPreferences.
     *
     * @param records The list of records to persist.
     */
    private void saveAll(List<AttendanceRecord> records) {
        JSONArray array = new JSONArray();
        for (AttendanceRecord r : records) {
            try {
                JSONObject obj = new JSONObject();
                obj.put("eventName", r.getEventName());
                obj.put("building", r.getBuilding());
                obj.put("roomName", r.getRoomName());
                obj.put("timestamp", r.getTimestamp());
                array.put(obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // Commit changes to SharedPreferences asynchronously.
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply();
    }
}