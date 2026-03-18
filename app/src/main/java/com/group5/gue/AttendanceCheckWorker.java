package com.group5.gue;

import android.content.Context;
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

    public AttendanceCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();

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