package com.group5.gue;

import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.group5.gue.data.model.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Manages all map marker and floor-level logic for the TU/e map.
 * Owns the active markers list, floor state, and all marker rendering.
 */
public class MarkerManager {

    private GoogleMap mMap;
    private List<Annotation> annotationList;

    private int currentFloor = -2; // -2 = buildings overview
    private final List<Marker> activeMarkers = new ArrayList<>();

    private final Button btnFloorUp;
    private final Button btnFloorDown;
    private final TextView tvFloorLevel;

    public MarkerManager(Button btnFloorUp, Button btnFloorDown, TextView tvFloorLevel) {
        this.btnFloorUp = btnFloorUp;
        this.btnFloorDown = btnFloorDown;
        this.tvFloorLevel = tvFloorLevel;

        btnFloorUp.setOnClickListener(v -> {
            if (currentFloor < 8) {
                currentFloor++;
                updateFloorLevel(null, null);
            }
        });

        btnFloorDown.setOnClickListener(v -> {
            if (currentFloor > -2) {
                currentFloor--;
                updateFloorLevel(null, null);
            }
        });
    }

    /** Called once the Google Map is ready. */
    public void attachMap(GoogleMap map) {
        this.mMap = map;
        if (annotationList != null && !annotationList.isEmpty()) {
            updateFloorLevel(null, null);
        }
    }

    /** Called when a fresh annotation list has been fetched from the database. */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotationList = annotations;
        if (mMap != null) {
            updateFloorLevel(null, null);
        }
    }

    public List<Annotation> getAnnotationList() {
        return annotationList;
    }

    public List<Marker> getActiveMarkers() {
        return activeMarkers;
    }

    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * Switches the floor view to match the given location string and places a red
     * marker at that location.
     *
     * @param location e.g. "Atlas 1.100" or "Atlas"
     */
    public void markLocation(String location) {
        if (annotationList == null) return;

        String[] parts = parseLocation(location);
        if (parts == null) {
            Log.e("MAP_DEBUG", "Invalid location format: " + location);
            return;
        }

        String building = parts[0];
        String room = parts.length > 1 ? parts[1] : null;

        Annotation target = findAnnotation(building, room);

        if (target == null) {
            Log.d("MAP_DEBUG", "Location not found in database: " + location);
            return;
        }

        currentFloor = target.getLevel() == null ? -2 : Integer.parseInt(target.getLevel());
        updateFloorLevel(target.getBuilding(), target.getRoomName());

        LatLng pos = new LatLng(target.getLatitude(), target.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f));

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(target.getBuilding())
                .snippet(target.getRoomName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        if (marker != null) {
            marker.setTag(target.getId());
        }
    }

    /**
     * Updates the displayed floor and re-renders markers.
     *
     * @param withoutBuilding building to exclude (null = show all)
     * @param withoutRoom     room to exclude within that building (null = show all)
     */
    public void updateFloorLevel(String withoutBuilding, String withoutRoom) {
        if (currentFloor == -2) {
            tvFloorLevel.setText("Buildings");
        } else {
            tvFloorLevel.setText("Level " + currentFloor);
        }

        showMarkersForFloor(withoutBuilding, withoutRoom);

        btnFloorUp.setEnabled(currentFloor < 8);
        btnFloorDown.setEnabled(currentFloor > -2);
        btnFloorUp.setAlpha(currentFloor < 8 ? 1f : 0.5f);
        btnFloorDown.setAlpha(currentFloor > -2 ? 1f : 0.5f);
    }

    /**
     * Updates ONLY the markers on the map to show the rooms/buildings corresponding to
     * {@link MarkerManager#currentFloor}.
     *
     * @param withoutBuilding string for a building name that is selected not to show. Null
     *                        if all buildings should be shown.
     * @param withoutRoom     if there is a room number associated with the lecture place, the room
     *                        number can be passed. This way room in withoutBuilding with number
     *                        withoutRoom will not be shown on the map. Null if there is no number
     *                        for the location or all markers should be shown.
     */
    private void showMarkersForFloor(String withoutBuilding, String withoutRoom) {
        if (mMap == null || annotationList == null) return;

        mMap.clear();
        activeMarkers.clear();

        for (Annotation annotation : annotationList) {
            String levelStr = annotation.getLevel();

            // Buildings layer
            if (levelStr == null && currentFloor == -2
                    && !Objects.equals(annotation.getBuilding(), withoutBuilding)) {
                addBlueMarker(annotation);
                continue;
            }

            // Rooms layer
            if (levelStr != null) {
                try {
                    int annotationLevel = Integer.parseInt(levelStr);
                    boolean isExcluded = Objects.equals(annotation.getBuilding(), withoutBuilding)
                            && Objects.equals(annotation.getRoomName(), withoutRoom);

                    if (annotationLevel == currentFloor && currentFloor > -2 && !isExcluded) {
                        addBlueMarker(annotation);
                    }
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Adds a blue-colored marker to the map for the given annotation.
     * <p>
     * The marker is positioned using the annotation's latitude and longitude,
     * and displays the building name as the title and the room name as the snippet.
     * The marker is visually distinguished using a blue hue.
     * <p>
     * If successfully created, the marker is tagged with the annotation ID and
     * stored in the list of active markers for later management.
     *
     * @param annotation The {@link Annotation} containing location and display information.
     */
    private void addBlueMarker(Annotation annotation) {
        LatLng pos = new LatLng(annotation.getLatitude(), annotation.getLongitude());
        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(annotation.getBuilding())
                .snippet(annotation.getRoomName())
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
        if (marker != null) {
            marker.setTag(annotation.getId());
            activeMarkers.add(marker);
        }
    }

    /**
     * Finds an {@link Annotation} based on building and optional room name.
     * <p>
     * The method first attempts to find an exact match using both building and room name.
     * If no exact match is found or the room is {@code null}, it falls back to searching
     * for a match based on the building name only.
     * <p>
     * String comparisons are case-insensitive.
     *
     * @param building The name of the building to search for.
     * @param room     The room name to match (may be {@code null}).
     * @return The matching {@link Annotation}, or {@code null} if no match is found.
     */
    private Annotation findAnnotation(String building, String room) {
        // Exact match first
        if (room != null) {
            for (Annotation a : annotationList) {
                if (building.equalsIgnoreCase(a.getBuilding())
                        && room.equalsIgnoreCase(a.getRoomName())) {
                    return a;
                }
            }
        }
        // Building-only fallback
        for (Annotation a : annotationList) {
            if (building.equalsIgnoreCase(a.getBuilding())) {
                return a;
            }
        }
        return null;
    }

    /**
     * Parses a full location string into a building name and an optional room number,
     * trimming whitespace and returning {@code null} if the input is blank or null.
     *
     * @param location the full location string (e.g. {@code "Atlas 1.100"} or {@code "Atlas"})
     * @return a string array where {@code [0]} is the building name and {@code [1]} is the
     *         room number if present; or {@code null} if the input is null or blank
     */
    static String[] parseLocation(String location) {
        if (location == null || location.trim().isEmpty()) return null;
        String[] tokens = location.trim().split("\\s+");
        return tokens.length == 1 ? new String[]{tokens[0]} : new String[]{tokens[0], tokens[1]};
    }
}