package com.group5.gue;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;
import com.group5.gue.data.annotation.AnnotationRepository;
import com.group5.gue.data.model.Annotation;
import com.group5.gue.data.model.Role;
import com.group5.gue.data.model.User;

/**
 * Handles all admin map interactions: placing new markers and deleting existing ones.
 * Only attaches listeners when the user has the ADMIN role.
 */
public class AdminMapManager {

    public interface MarkerRefreshListener {
        /** Called after a successful create or delete so the caller can re-fetch annotations. */
        void onAnnotationsChanged();
    }

    private final Context context;
    private final AnnotationRepository repository;
    private final User user;
    private final int currentFloor;
    private final MarkerRefreshListener refreshListener;

    private boolean isAddingMarker = false;
    private Snackbar instructionSnackbar;

    /**
     * @param context         application or fragment context
     * @param repository      data source for annotations
     * @param user            the currently authenticated user
     * @param currentFloor    current floor level at the time of marker placement
     * @param refreshListener called after successful DB operations
     */
    public AdminMapManager(Context context,
                           AnnotationRepository repository,
                           User user,
                           int currentFloor,
                           MarkerRefreshListener refreshListener) {
        this.context = context;
        this.repository = repository;
        this.user = user;
        this.currentFloor = currentFloor;
        this.refreshListener = refreshListener;
    }

    /**
     * Returns true if the current user is an admin.
     */
    public boolean isAdmin() {
        return user != null && user.getRole() == Role.ADMIN;
    }

    /**
     * Enters marker-adding mode and shows a Snackbar instruction.
     * Call this from the FAB's click handler.
     *
     * @param rootView the view used to anchor the Snackbar
     */
    public void beginAddingMarker(View rootView) {
        isAddingMarker = true;
        instructionSnackbar = Snackbar.make(
                rootView, "Tap on the map to place a marker", Snackbar.LENGTH_INDEFINITE);

        View snackbarView = instructionSnackbar.getView();
        FrameLayout.LayoutParams params =
                (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
        params.gravity = Gravity.TOP;
        params.topMargin = 150;
        snackbarView.setLayoutParams(params);

        instructionSnackbar.setAction("Cancel", v -> {
            isAddingMarker = false;
            instructionSnackbar.dismiss();
        });
        instructionSnackbar.show();
    }

    /**
     * Attaches map click and info-window-click listeners for admin actions.
     * Safe to call on any map; does nothing if user is not admin.
     *
     * @param mMap the ready Google Map
     */
    public void attachListeners(GoogleMap mMap) {
        mMap.setOnMapClickListener(latLng -> {
            if (isAdmin() && isAddingMarker) {
                if (instructionSnackbar != null) instructionSnackbar.dismiss();
                showAddMarkerDialog(mMap, latLng);
                isAddingMarker = false;
            }
        });

        mMap.setOnInfoWindowClickListener(marker -> {
            if (!isAdmin()) return;
            new AlertDialog.Builder(context)
                    .setTitle("Delete Location")
                    .setMessage("Are you sure you want to delete " + marker.getTitle() + "?")
                    .setPositiveButton("Delete", (dialog, which) ->
                            deleteMarker(marker))
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    /**
     * Displays a dialog that allows the user to add a new marker (location) on the map.
     * The dialog collects information such as building name, room number, and level.
     * <p>
     * When the user confirms the input:
     * <ul>
     *     <li>A new {@link Annotation} is created with the provided details and the given coordinates.</li>
     *     <li>The annotation is stored in the repository.</li>
     *     <li>If successful, a corresponding {@link Marker} is added to the {@link GoogleMap}.</li>
     *     <li>A callback is triggered to refresh annotations on the map.</li>
     * </ul>
     * <p>
     * The level input field is only shown when a room number is entered. If no level is provided,
     * the current floor is used (if available).
     *
     * @param mMap   The {@link GoogleMap} instance where the marker will be added.
     * @param latLng The geographic coordinates where the marker should be placed.
     */
    private void showAddMarkerDialog(GoogleMap mMap, LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add New Location");

        View dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_add_marker, null);
        EditText inputName  = dialogView.findViewById(R.id.input_building_name);
        EditText inputRoom  = dialogView.findViewById(R.id.input_room_number);
        EditText inputLevel = dialogView.findViewById(R.id.input_level);

        inputRoom.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLevel.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Save", (dialog, which) -> {
            String name     = inputName.getText().toString().trim();
            String room     = inputRoom.getText().toString().trim();
            String levelStr = inputLevel.getText().toString().trim();

            if (name.isEmpty()) return;

            String level;
            if (room.isEmpty()) {
                level = null;
            } else if (levelStr.isEmpty()) {
                level = currentFloor == -2 ? null : String.valueOf(currentFloor);
            } else {
                level = levelStr;
            }

            Annotation newAnnotation = new Annotation(
                    0L, null, name, room, level,
                    latLng.latitude, latLng.longitude, user.getId());

            repository.create(newAnnotation, created -> {
                if (created != null) {
                    Marker marker = mMap.addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(created.getBuilding())
                            .snippet(created.getRoomName()));
                    if (marker != null) marker.setTag(created.getId());

                    Toast.makeText(context, "Saved to database", Toast.LENGTH_SHORT).show();
                    refreshListener.onAnnotationsChanged();
                } else {
                    Log.e("MAP_ADMIN", "Create failed: " + name);
                    Toast.makeText(context, "Save failed", Toast.LENGTH_SHORT).show();
                }
                return null;
            });
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Deletes a marker from both the map and the underlying data repository.
     * <p>
     * The marker's associated annotation ID is retrieved from its tag. If the ID exists,
     * a delete request is sent to the repository. Upon successful deletion:
     * <ul>
     *     <li>The marker is removed from the {@link GoogleMap}.</li>
     *     <li>A refresh callback is triggered to update annotations.</li>
     *     <li>A confirmation message is shown to the user.</li>
     * </ul>
     * If the deletion fails, an error message is displayed instead.
     *
     * @param marker The {@link Marker} to be deleted. Its tag must contain the corresponding annotation ID.
     */
    private void deleteMarker(Marker marker) {
        Long id = (Long) marker.getTag();
        if (id == null) return;

        repository.delete(id, success -> {
            if (success) {
                marker.remove();
                refreshListener.onAnnotationsChanged();
                Toast.makeText(context, "Deleted from database", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Delete failed", Toast.LENGTH_SHORT).show();
            }
            return null;
        });
    }
}