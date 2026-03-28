package com.group5.gue;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.group5.gue.data.annotation.AnnotationRepository;
import com.group5.gue.data.model.Annotation;

import java.util.List;
import java.util.function.Consumer;

/**
 * Checks whether the user's current location is within a given radius of a
 * building or room defined by an {@link Annotation}.
 *
 * <p>Location is retrieved via the {@link FusedLocationProviderClient}, and
 * annotation coordinates are sourced from {@link AnnotationRepository}. Both
 * operations are asynchronous, so all results are delivered through a
 * {@link java.util.function.Consumer} callback rather than returned directly.
 *
 * <p>{@link Manifest.permission#ACCESS_FINE_LOCATION} must be granted before
 * calling {@link #check}; the callback will receive {@code false} immediately
 * if it is not.
 */
public class ProximityChecker {

    private static final String TAG = "ProximityChecker";

    private final Context context;
    private final AnnotationRepository repository;

    /**
     * Creates a new {@code ProximityChecker} bound to the application context.
     *
     * <p>Using the application context (rather than an Activity context) prevents
     * memory leaks when the check outlives the calling component.
     *
     * @param context any {@link Context}; the application context is extracted
     *                internally via {@link Context#getApplicationContext()}
     */
    public ProximityChecker(Context context) {
        this.context = context.getApplicationContext();
        this.repository = AnnotationRepository.Companion.getInstance();
    }

    /**
     * Determines whether the user's current location is within a given proximity
     * of a specified building and room.
     *
     * <p>If annotations are not yet loaded, fetches them first. If no exact
     * building+room match is found, falls back to building-only match.
     * Returns the result via a callback since both location fetching and
     * annotation loading are asynchronous.
     *
     * <p>The callback receives {@code false} if location permission is not granted,
     * the user's location cannot be retrieved, or the building is not found.
     *
     * @param buildingName    the building name, matched case-insensitively
     * @param roomName        the room name, matched case-insensitively
     * @param proximityMeters radius in meters to consider "nearby"
     * @param annotationList  preloaded annotation list, or {@code null} to fetch fresh
     * @param callback        receives {@code true} if user is within range,
     *                        {@code false} otherwise
     */
    public void check(String buildingName, String roomName, double proximityMeters,
                      List<Annotation> annotationList, Consumer<Boolean> callback) {

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            callback.accept(false);
            return;
        }

        if (annotationList != null && !annotationList.isEmpty()) {
            performCheck(buildingName, roomName, proximityMeters, annotationList, callback);
        } else {
            repository.getAll(new kotlin.jvm.functions.Function1<List<Annotation>, kotlin.Unit>() {
                @Override
                public kotlin.Unit invoke(List<Annotation> fetchedAnnotations) {
                    performCheck(buildingName, roomName, proximityMeters,
                            fetchedAnnotations, callback);
                    return kotlin.Unit.INSTANCE;
                }
            });
        }
    }

    /**
     * Resolves the user's last known location and compares it against the
     * coordinates of the best-matching annotation for the given building and room.
     *
     * <p>Annotation matching is attempted in two passes:
     * <ol>
     *   <li>Exact match on both building name and room name (case-insensitive).</li>
     *   <li>Building-only match (case-insensitive) if no room match is found.</li>
     * </ol>
     *
     * <p>Distance is calculated using
     * {@link android.location.Location#distanceBetween} and compared against
     * {@code proximityMeters}. The callback receives {@code false} if either
     * fine or coarse location permission is missing, the last known location is
     * {@code null}, or no matching annotation is found.
     *
     * @param buildingName    the building name to match, case-insensitively
     * @param roomName        the room name to match, case-insensitively
     * @param proximityMeters the maximum distance in meters to be considered nearby
     * @param annotations     the full list of annotations to search through;
     *                        must not be {@code null}
     * @param callback        receives {@code true} if the user is within
     *                        {@code proximityMeters} of the target, {@code false} otherwise
     */
    private void performCheck(String buildingName, String roomName, double proximityMeters,
                              List<Annotation> annotations, Consumer<Boolean> callback) {

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Location permission not granted");
            callback.accept(false);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location == null) {
                Log.w(TAG, "Last known location is null");
                callback.accept(false);
                return;
            }

            Annotation target = null;

            // Try exact building + room match first
            for (Annotation annotation : annotations) {
                if (buildingName.equalsIgnoreCase(annotation.getBuilding()) &&
                        roomName.equalsIgnoreCase(annotation.getRoomName())) {
                    target = annotation;
                    break;
                }
            }

            // Fall back to building-only match
            if (target == null) {
                for (Annotation annotation : annotations) {
                    if (buildingName.equalsIgnoreCase(annotation.getBuilding())) {
                        target = annotation;
                        break;
                    }
                }
            }

            if (target == null) {
                Log.d(TAG, "No annotation found for: " + buildingName + " " + roomName);
                callback.accept(false);
                return;
            }

            float[] results = new float[1];
            android.location.Location.distanceBetween(
                    location.getLatitude(), location.getLongitude(),
                    target.getLatitude(), target.getLongitude(),
                    results
            );

            Log.d(TAG, "Distance to " + buildingName + ": " + results[0] + "m");
            callback.accept(results[0] <= proximityMeters);
        });
    }
}