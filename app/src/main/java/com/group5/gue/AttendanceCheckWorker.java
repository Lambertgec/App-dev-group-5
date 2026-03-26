package com.group5.gue;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.group5.gue.data.attendance.AttendanceRepository;
import com.group5.gue.data.model.AttendanceRecord;

import java.util.ArrayList;

public class AttendanceCheckWorker extends Worker {

    private static final String TAG = "AttendanceWorker";
    // Proximity threshold in meters
    private static final double PROXIMITY_METERS = 100.0;

    private static final String PREFS_LECTURE = "lecture_prefs";
    private static final String KEY_LECTURE_END_TIME = "lecture_end_time";
    private static final String KEY_CODE_VERIFIED = "attendance_verified";

    private static final String KEY_IN_ATTENDANCE = "in_attendance";


    public AttendanceCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE);

        // 1. Check if the user has entered a valid code for the current lecture
        boolean isVerified = prefs.getBoolean(KEY_CODE_VERIFIED, false);
        long lectureEndTime = prefs.getLong(KEY_LECTURE_END_TIME, 0);

        if (!isVerified || System.currentTimeMillis() > lectureEndTime) {
            if (isVerified && System.currentTimeMillis() > lectureEndTime) {
                // Reset verification if the lecture has already ended
                prefs.edit().putBoolean(KEY_CODE_VERIFIED, false).apply();
            }
            Log.d(TAG, "Attendance not verified via code or lecture ended, skipping check");
            return Result.success();
        }

        CalendarHandler calendarHandler;
        try {
            calendarHandler = new CalendarHandler(context.getContentResolver());
            if (CalendarHandler.selectedCalendar != null) {
                calendarHandler.setCalendar(CalendarHandler.selectedCalendar);
            }
        } catch (Exception e) {
            Log.e(TAG, "CalendarHandler init failed", e);
            return Result.failure();
        }

        ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();
        if (ongoingEvents.isEmpty()) {
            Log.d(TAG, "No ongoing events, skipping check");
            return Result.success();
        }

        Event event = ongoingEvents.get(0);
        String location = event.getLocation();

        if (location == null || location.isEmpty()) {
            Log.d(TAG, "Event has no location, skipping check");
            return Result.success();
        }

        String[] parts = location.split(" ");
        if (parts.length < 2) {
            Log.d(TAG, "Could not parse location: " + location);
            return Result.success();
        }

        String building = parts[0];
        String room = parts[1];

        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        boolean[] isNearby = {false};

        new ProximityChecker(context).check(building, room, PROXIMITY_METERS, null, result -> {
            isNearby[0] = result;
            latch.countDown();
        });

        try {
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Proximity check timed out", e);
            return Result.failure();
        }

        if (isNearby[0]) {

            prefs.edit().putBoolean(KEY_IN_ATTENDANCE, true).apply();

            AttendanceRecord record = new AttendanceRecord(
                    event.title,
                    building,
                    room,
                    System.currentTimeMillis()
            );

            AttendanceRepository repo = AttendanceRepository.getInstance(context);
            boolean saved = repo.saveIfNotDuplicate(record);
            Log.d(TAG, saved ? "Attendance recorded for: " + event.title
                    : "Duplicate, skipping: " + event.title);
        }

        return Result.success();
    }
}
