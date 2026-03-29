package com.group5.gue;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.group5.gue.api.BaseRepository;
import com.group5.gue.blocking.AppBlockingManager;
import com.group5.gue.data.PermissionHandler;
import com.group5.gue.data.attendance.AttendanceRepository;
import com.group5.gue.data.model.AttendanceRecord;
import com.group5.gue.data.model.User;
import com.group5.gue.data.user.UserRepository;

import java.util.ArrayList;

/**
 * AttendanceCheckWorker is a background worker that periodically checks if a user
 * is attending a lecture.
 * 
 * <p>The worker performs several checks:
 * 1. Checks if the lecture code was verified and if the lecture is still ongoing.
 * 2. Queries the user's calendar for ongoing events.
 * 3. Verifies the user's physical proximity to the lecture's recorded location.
 * 4. Records attendance locally and updates user's score/points if verification is successful.
 * </p>
 *
 */
public class AttendanceCheckWorker extends Worker {

    /** Tag used for logging worker activities. */
    private static final String TAG = "AttendanceWorker";
    // Proximity threshold in meters
    private static final double PROXIMITY_METERS = 100.0;

    /** Name of the shared preferences file for lecture state. */
    private static final String PREFS_LECTURE = "lecture_prefs";
    
    /** Key for the end time of the current verified lecture. */
    private static final String KEY_LECTURE_END_TIME = "lecture_end_time";
    
    /** Key for the boolean verification status. */
    private static final String KEY_CODE_VERIFIED = "attendance_verified";

    /** Key to track if the user is currently considered "in attendance". */
    private static final String KEY_IN_ATTENDANCE = "in_attendance";

    /**
     * Constructs the worker.
     *
     * @param context The application context.
     * @param params Parameters for this worker's execution.
     */
    public AttendanceCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Executes the background work of checking attendance.
     * This method runs on a background thread provided by WorkManager.
     * 
     * @return The result of the work (Success or Failure).
     */
    @NonNull
    @Override
    public Result doWork() {
        // Retrieve context and initialized managers
        Context context = getApplicationContext();
        SharedPreferences prefs = context.getSharedPreferences(PREFS_LECTURE, Context.MODE_PRIVATE);
        AppBlockingManager blockingManager = new AppBlockingManager(context);

        // Check if the user has entered a valid code for the current lecture
        boolean isVerified = prefs.getBoolean(KEY_CODE_VERIFIED, false);
        long lectureEndTime = prefs.getLong(KEY_LECTURE_END_TIME, 0);

        // If not verified or lecture ended, cleanup and return success (no work needed)
        if (!isVerified || System.currentTimeMillis() > lectureEndTime) {
            if (isVerified && System.currentTimeMillis() > lectureEndTime) {
                // Reset verification if the lecture has already ended
                prefs.edit().putBoolean(KEY_CODE_VERIFIED, false).apply();
                blockingManager.stopBlockingService();
            }
            Log.d(TAG, "Attendance not verified via code or lecture ended, skipping check");
            return Result.success();
        }

        // Initialize calendar handler to find the scheduled lecture
        CalendarHandler calendarHandler;
        try {
            calendarHandler = new CalendarHandler(context.getContentResolver());
            if (CalendarHandler.selectedCalendar != null) {
                calendarHandler.setCalendar(CalendarHandler.selectedCalendar);
            }
        } catch (Exception e) {
            // Log failure if calendar access fails
            Log.e(TAG, "CalendarHandler init failed", e);
            return Result.failure();
        }

        // Fetch ongoing events from the user's selected calendar
        ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();
        if (ongoingEvents.isEmpty()) {
            // No event is currently happening according to the calendar
            Log.d(TAG, "No ongoing events, skipping check");
            return Result.success();
        }

        // Focus on the first event found
        Event event = ongoingEvents.get(0);
        String location = event.getLocation();

        // Ensure the event actually has location data to check against
        if (location == null || location.isEmpty()) {
            Log.d(TAG, "Event has no location, skipping check");
            return Result.success();
        }

        // Parse building and room from the location string (expected format: "Building Room")
        String building = new String();
        String room = new String();
        String[] parts = location.split(" ");

        switch (parts.length) {
            case 0:
                Log.d(TAG, "Could not parse location: " + location);
                return Result.success();
            case 1:
                building = parts[0];
                room = "";
                break;
            default:
                building = parts[0];
                room = parts[1];
        }

        // Synchronize with an asynchronous proximity check using a Latch
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        boolean[] isNearby = {false};

        // Trigger physical location verification
        new ProximityChecker(context).check(building, room, PROXIMITY_METERS, null, result -> {
            // Result callback: update state and release latch
            isNearby[0] = result;
            latch.countDown();
        });

        try {
            // Block worker execution until proximity check completes (timeout after 10s)
            latch.await(10, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Proximity check timed out", e);
            return Result.failure();
        }

        // If the user is physically at the lecture location
        if (isNearby[0]) {
            // Update preferences to reflect current attendance status
            prefs.edit().putBoolean(KEY_IN_ATTENDANCE, true).apply();

            // Ensure app blocking is active since they are in a lecture
            if (!blockingManager.isServiceRunning()) {
                blockingManager.startBlockingService();
            }

            // Create a record of this attendance
            AttendanceRecord record = new AttendanceRecord(
                    event.title,
                    building,
                    room,
                    System.currentTimeMillis()
            );

            // Save the record to local database if it hasn't been saved yet
            AttendanceRepository repo = AttendanceRepository.getInstance(context);
            boolean saved = repo.saveIfNotDuplicate(record);

            // Award points for attending
            if (saved) {
                User user = UserRepository.Companion.getInstance().getCachedUser();
                if (user != null) {
//                    lecture time in minutes
                    int pts = (int) (event.endTime - event.startTime) / 60000;

                    // Create updated user object with new score
                    User updatedUser = user.copy(
                            user.getId(),
                            user.getName(),
                            user.getScore() + pts,
                            user.isAdmin()
                    );

                    // Push user update to the remote repository
                    UserRepository.Companion.getInstance().updateUser(updatedUser, result -> {
                        Log.d(TAG, result.toString());
                    });
                }
            }

            Log.d(TAG, saved ? "Attendance recorded for: " + event.title
                    : "Duplicate, skipping: " + event.title);
        }

        return Result.success();
    }
}
