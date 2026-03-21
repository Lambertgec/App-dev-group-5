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

public class AttendanceRepository {

    private static final String PREFS_NAME = "attendance_prefs";
    private static final String KEY_RECORDS = "attendance_records";

    private static AttendanceRepository instance;
    private final SharedPreferences prefs;

    private AttendanceRepository(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

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
     * Returns all stored attendance records.
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
            e.printStackTrace();
        }

        return records;
    }

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
        prefs.edit().putString(KEY_RECORDS, array.toString()).apply();
    }
}