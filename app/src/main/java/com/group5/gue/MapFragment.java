package com.group5.gue;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import android.content.res.Resources;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.group5.gue.data.annotation.AnnotationRepository;
import com.group5.gue.data.model.Annotation;

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.group5.gue.data.user.UserRepository;
import com.group5.gue.data.model.User;
import com.group5.gue.data.model.Role;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.Objects;
import android.widget.ArrayAdapter;
import android.content.Context;
/**
 * A simple {@link Fragment} subclass for displaying the TU/e map with markers.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    LatLng tueCampus = new LatLng(51.448, 5.489);
    private TextView eventBar;
    private CalendarHandler calendarHandler;

    // Admin state
    private boolean isAddingMarker = false;
    private Snackbar instructionSnackbar;

    AnnotationRepository repository = AnnotationRepository.Companion.getInstance();
    private List<Annotation> annotationList;

    private int currentFloor = -2; // starting level
    private Button btnFloorUp, btnFloorDown;
    private TextView tvFloorLevel;
    private boolean mapReady = false;
    private AutoCompleteTextView buildingSearch;
    private List<String> buildingNames = new ArrayList<>();
    private final List<Marker> activeMarkers = new ArrayList<>();

    UserRepository userRepository = UserRepository.Companion.getInstance();
    User user;

    private List<Marker> getActiveMarkers() {
        return activeMarkers;
    }

    public MapFragment() {
        user = userRepository.getCachedUser();
        // Required empty public constructor
    }

    /**
     * Determines whether the user's current location is within a given proximity
     * of a specified building and room in the annotation database.
     * <p>
     * The method fetches the user's last known location via {@link FusedLocationProviderClient},
     * looks up the building and room in {@link #annotationList}, then calculates the
     * straight-line distance between the two points. Returns the result via a callback
     * since location fetching is asynchronous.
     * <p>
     * If the building/room is not found in the database, the user's location cannot
     * be retrieved, or location permission is not granted, the callback receives {@code false}.
     * If no exact building+room match is found, falls back to matching building only.
     * Usage: {@code isUserNearLocation("Atlas", "1.100", 20, isNearby -> {
     *      Log.d("MAP_PROXIMITY", "isNearby: " + isNearby);
     *     });}
     *
     * @param buildingName    the name of the building to check proximity to,
     *                        matched case-insensitively against {@link Annotation#getBuilding()}
     * @param roomName        the room name within the building, matched case-insensitively
     *                        against {@link Annotation#getRoomName()}
     * @param proximityMeters the radius in meters within which the user is considered
     *                        to be at the location
     * @param callback        receives {@code true} if the user is within {@code proximityMeters}
     *                        of the specified location, {@code false} otherwise
     */
    public void isUserNearLocation(String buildingName, String roomName,
                                   double proximityMeters, Consumer<Boolean> callback) {
        new ProximityChecker(requireContext())
                .check(buildingName, roomName, proximityMeters, annotationList, callback);
    }

    /* -------------------------------- Override Functions ------------------------------------- */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(requireActivity());
        Button btnCenterTue = view.findViewById(R.id.btn_center_tue);
        btnFloorUp = view.findViewById(R.id.btn_floor_up);
        btnFloorDown = view.findViewById(R.id.btn_floor_down);
        tvFloorLevel = view.findViewById(R.id.tv_floor_level);

        // Displays the even bar at the top of the fragment
        eventBar = view.findViewById(R.id.event_bar);
        try {
            calendarHandler = new CalendarHandler(requireActivity());

            if (CalendarHandler.selectedCalendar != null) {
                calendarHandler.setCalendar(CalendarHandler.selectedCalendar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateEventBar();

        // Role check
        FloatingActionButton addAnnotationBtn = view.findViewById(R.id.add_annotation);

        // Check for ADMIN role
        if (isAdmin()) {
            addAnnotationBtn.setVisibility(View.VISIBLE);
            addAnnotationBtn.setOnClickListener(v -> {
                isAddingMarker = true;
                // Use a Snackbar that stays until dismissed
                instructionSnackbar = Snackbar.make(view, "Tap on the map to place a marker",
                        Snackbar.LENGTH_INDEFINITE);

                // Position Snackbar at the top
                View snackbarView = instructionSnackbar.getView();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)
                        snackbarView.getLayoutParams();
                params.gravity = Gravity.TOP;
                params.topMargin = 150;
                snackbarView.setLayoutParams(params);

                instructionSnackbar.setAction("Cancel", v1 -> {
                    isAddingMarker = false;
                    instructionSnackbar.dismiss();
                });
                instructionSnackbar.show();
            });
        } else {
            addAnnotationBtn.setVisibility(View.GONE);
        }

        //Displays the map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Fetches building data from the database
        fetchAnnotations();

        updateFloorLevel(null, null);

        buildingSearch = view.findViewById(R.id.building_search);
        setupBuildingSearch();

        // Centers on TU/e when button is clicked
        btnCenterTue.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tueCampus, 16f));
            }
        });

        // Buttons to change floor
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        // used so that the 'center at user's location' button is not covered
        mMap.setPadding(
                0,   // left
                250, // top
                0,   // right
                0    // bottom
        );

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tueCampus, 16f));
        // Add markers if annotations are already loaded
        if (annotationList != null && !annotationList.isEmpty()) {
            updateFloorLevel(null, null);
        }

        // Set custom info window
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) {
                return null;
            }

            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                View window = getLayoutInflater().inflate(R.layout.custom_info_window, null);

                TextView title = window.findViewById(R.id.info_window_title);
                TextView snippet = window.findViewById(R.id.info_window_snippet);
                ImageView deleteIcon = window.findViewById(R.id.delete);

                title.setText(marker.getTitle());
                snippet.setText(marker.getSnippet());

                // Show 'x' only for admins
                if (isAdmin()) {
                    deleteIcon.setVisibility(View.VISIBLE);
                } else {
                    deleteIcon.setVisibility(View.GONE);
                }

                return window;
            }
        });

        // Adding listeners for Admin actions
        setupAdminListeners();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }

    /* ---------------------------- Markers' Display Functions --------------------------------- */

    /**
     * Fetches the building annotations from the Supabase database and stores them in a list
     * {@link MapFragment#annotationList}.
     */
    private void fetchAnnotations() {
        repository.getAll(new kotlin.jvm.functions.Function1<List<Annotation>, kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke(List<Annotation> annotations) {
                annotationList = annotations;

                if (mapReady) {
                    updateFloorLevel(null, null);
                }

                // Populate building search suggestions
                buildingNames.clear();
                for (Annotation a : annotations) {
                    if (a.getLevel() == null && a.getBuilding() != null
                            && !buildingNames.contains(a.getBuilding())) {
                        buildingNames.add(a.getBuilding());
                    }
                }
                setupBuildingSearch();

                return kotlin.Unit.INSTANCE;
            }
        });
    }

    /**
     * Sets up the building search bar with autocomplete suggestions drawn from
     * {@link #annotationList}. Only top-level building annotations (those with
     * no floor level) are included as suggestions.
     * <p>
     * When the user selects a suggestion, the map centers on the building and
     * opens its info window, mimicking a manual marker tap. The search bar is
     * cleared after selection.
     */
    private void setupBuildingSearch() {
        if (buildingSearch == null || buildingNames.isEmpty()) return;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                buildingNames
        );
        buildingSearch.setAdapter(adapter);

        buildingSearch.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            buildingSearch.setText("");
            buildingSearch.clearFocus();

            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm =
                    (android.view.inputmethod.InputMethodManager)
                            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(buildingSearch.getWindowToken(), 0);

            // Find the annotation for the selected building
            if (annotationList == null) return;
            for (Annotation annotation : annotationList) {
                if (selected.equalsIgnoreCase(annotation.getBuilding())
                        && annotation.getLevel() == null) {

                    LatLng pos = new LatLng(annotation.getLatitude(), annotation.getLongitude());

                    // Switch to buildings floor and center
                    currentFloor = -2;
                    updateFloorLevel(null, null);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f),
                            new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    // Open info window after camera settles
                                    if (mMap != null) {
                                        for (com.google.android.gms.maps.model.Marker m :
                                                getActiveMarkers()) {
                                            if (selected.equalsIgnoreCase(m.getTitle())) {
                                                m.showInfoWindow();
                                                break;
                                            }
                                        }
                                    }
                                }
                                @Override
                                public void onCancel() {}
                            });
                    break;
                }
            }
        });
    }

    /**
     * Updates the building markers displayed depending on the current floor level.
     */
    private void updateFloorLevel(String withoutBuilding, String withoutRoom) {
        if (currentFloor == -2) {
            tvFloorLevel.setText("Buildings");
        } else tvFloorLevel.setText("Level " + currentFloor);

        // Update markers for this floor
        showMarkersForFloor(withoutBuilding, withoutRoom);

        // Enable/disable buttons depending on floor
        btnFloorUp.setEnabled(currentFloor < 8);
        btnFloorDown.setEnabled(currentFloor > -2);

        // Gray out disabled buttons
        btnFloorUp.setAlpha(currentFloor < 8 ? 1f : 0.5f);
        btnFloorDown.setAlpha(currentFloor > -2 ? 1f : 0.5f);
    }

    /**
     * Updates ONLY the markers on the map to show the rooms/buildings corresponding to
     * {@link MapFragment#currentFloor}.
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

            // Case 1: buildings (no level)
            if ((levelStr == null) && currentFloor == -2 &&
                    !Objects.equals(annotation.getBuilding(), withoutBuilding)) {
                LatLng pos = new LatLng(annotation.getLatitude(), annotation.getLongitude());
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(annotation.getBuilding())
                        .snippet(annotation.getRoomName())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_BLUE
                        )));
                if (marker != null) {
                    marker.setTag(annotation.getId());
                    activeMarkers.add(marker);
                }
                continue;
            }

            // Case 2: rooms (level is numeric)
            if (levelStr != null) {
                try {
                    int annotationLevel = Integer.parseInt(levelStr);
                    if (annotationLevel == currentFloor && currentFloor > -2 &&
                            !(Objects.equals(annotation.getBuilding(), withoutBuilding) &&
                                    Objects.equals(annotation.getRoomName(), withoutRoom))) {
                        LatLng pos
                                = new LatLng(annotation.getLatitude(), annotation.getLongitude());
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(annotation.getBuilding())
                                .snippet(annotation.getRoomName())
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_BLUE
                                )));
                        if (marker != null) {
                            marker.setTag(annotation.getId());
                        }
                    }
                } catch (NumberFormatException e) {
                    // Skip rows with invalid level data
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Updates the floor level to match the one of location's, unless it is not in the database.
     * This then also marks in different color the location's coordinates on the map.
     * @param location - the location name that needs to be marked in different color.
     */
    private void markLocation(String location) {
        if (annotationList == null) return;

        String[] parts = location.split(" ");

        if (parts.length < 2) {
            Log.e("MAP_DEBUG", "Invalid location format: " + location);
            return;
        }

        String building = parts[0];
        String room = parts[1];

        Annotation currentLocation = null;

        for (Annotation annotation : annotationList) {
            if (building.equalsIgnoreCase(annotation.getBuilding()) &&
                    room.equalsIgnoreCase(annotation.getRoomName())) {
                currentLocation = annotation;
                break;
            }
        }

        if (currentLocation == null) {
            for (Annotation annotation : annotationList) {
                if (building.equalsIgnoreCase(annotation.getBuilding())) {
                    currentLocation = annotation;
                    break;
                }
            }
        }

        if (currentLocation == null) {
            Log.d("MAP_DEBUG", "There is no such location in the database: " + location);
            return;
        }

        if (currentLocation.getLevel() == null)
            currentFloor = -2;
        else
            currentFloor = Integer.parseInt(currentLocation.getLevel());

        updateFloorLevel(currentLocation.getBuilding(), currentLocation.getRoomName());

        LatLng pos = new LatLng(currentLocation.getLatitude(),
                currentLocation.getLongitude());

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(pos, 18f));

        Marker marker = mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title(currentLocation.getBuilding())
                .snippet(currentLocation.getRoomName())
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_RED
                )));
        if (marker != null) {
            marker.setTag(currentLocation.getId());
        }
    }

    /* ------------------------------ Events Bar Functions ------------------------------------ */
    /**
     * Updates the message displayed in the event bar at the top of the map.
     * The method checks the user's calendar (via CalendarHandler) to determine:
     * 1. If there is an event currently ongoing.
     * 2. If there is an upcoming event starting soon.
     * 3. If there are no relevant events.
     * Based on this information, the text in the event bar is updated to inform
     * the user where their lecture/event is happening or that there are no
     * upcoming events within the checked time window.
     * If the calendar cannot be accessed (e.g., permission not granted or
     * CalendarHandler not initialized), the user is prompted to give calendar
     * permission so the app can show the building of upcoming lectures.
     */
    private void updateEventBar() {
        if (calendarHandler == null) {
            eventBar.setText(getString(R.string.calendar_permission_message));
            return;
        }

        ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();

        if (!ongoingEvents.isEmpty()) {
            setEventBarClickable(getString(R.string.event_happening), ongoingEvents.get(0));
            return;
        }

        // Checks for events happening in 1 hour
        ArrayList<Event> upcoming = calendarHandler.getStartingSoon();

        if (!upcoming.isEmpty()) {
            setEventBarClickable(getString(R.string.event_starting_soon), upcoming.get(0));
        } else {
            eventBar.setText(R.string.no_event_soon);
        }
    }

    /**
     * Updates the eventBar with clickable location in event e, that marks the location
     * (if it is in the database) and centers it. This also updates the displayed floor.
     * @param text - text displayed in the eventBar.
     * @param e - event whose location needs to be displayed in the eventBar.
     */
    private void setEventBarClickable(String text, Event e)
    {
        if (e.getLocation() == null || e.getLocation().isEmpty()) {
            eventBar.setText(text + " (no location)");
            return;
        }

        String fullText = text + " " + e.getLocation();
        Log.d("MAP_DEBUG", e.getLocation());

        // Find where the location starts
        int start = fullText.indexOf(e.getLocation());
        int end = start + e.getLocation().length();

        SpannableString spannable = new SpannableString(fullText);

        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                markLocation(e.getLocation());
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        eventBar.setText(spannable);
        eventBar.setMovementMethod(LinkMovementMethod.getInstance());
        eventBar.setHighlightColor(Color.TRANSPARENT);
    }

    /* ------------------------------- Admin view Functions ------------------------------------ */
    /**
     * Sets up the map interaction listeners for admin users.
     * Configures two listeners:
     * <ul>
     *   <li>A map click listener that, when the admin is in marker-adding mode
     *       ({@link #isAddingMarker} is true), dismisses the instruction snackbar
     *       and opens the add marker dialog at the tapped location.</li>
     *   <li>An info window click listener that prompts the admin to confirm
     *       deletion of the tapped marker, then removes it from both the map
     *       and the database via {@link AnnotationRepository#delete}.</li>
     * </ul>
     * This method should only be called after the map is ready (i.e. from
     * {@link #onMapReady}).
     */
    private void setupAdminListeners() {
        mMap.setOnMapClickListener(latLng -> {
            if (isAdmin() && isAddingMarker) {

                if (instructionSnackbar != null) {
                    instructionSnackbar.dismiss();
                }

                showAddMarkerDialog(latLng);
                isAddingMarker = false;
            }
        });

        // Delete logic: Click a marker's info window to delete it (Admin only)
        mMap.setOnInfoWindowClickListener(marker -> {
            if (isAdmin()) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Location")
                        .setMessage("Are you sure you want to delete " + marker.getTitle() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            Long id = (Long) marker.getTag();

                            if (id != null) {
                                repository.delete(id, success -> {
                                    if (success) {
                                        marker.remove();
                                        fetchAnnotations(); // refresh from DB

                                        Toast.makeText(getContext(),
                                                "Deleted from database",
                                                Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(),
                                                "Delete failed",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    return null;
                                });
                            }
                            Toast.makeText(getContext(), "Marker removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    /**
     * Displays a dialog allowing an admin to save a new location marker at the
     * given coordinates.
     * The dialog prompts the admin for a building name and an optional room name
     * (snippet). On confirmation, a new {@link Annotation} is constructed with
     * the current floor level and the authenticated user's ID as the creator,
     * then persisted to the database via {@link AnnotationRepository#create}.
     * If the insert succeeds, the marker is added to the map and the annotation
     * list is refreshed. If it fails, an error is logged and a toast is shown.
     *
     * @param latLng the coordinates on the map where the new marker should be placed,
     *               as selected by the admin's tap
     */
    private void showAddMarkerDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Location");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_marker,
                (ViewGroup) getView(), false);
        final EditText inputName = viewInflated.findViewById(R.id.input_building_name);
        final EditText inputRoom = viewInflated.findViewById(R.id.input_room_number);
        final EditText inputLevel = viewInflated.findViewById(R.id.input_level);

        // Show level field only when a room number is entered
        inputRoom.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                inputLevel.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        builder.setView(viewInflated);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String room = inputRoom.getText().toString().trim();
            String levelStr = inputLevel.getText().toString().trim();

            if (!name.isEmpty()) {
                // Determine level: null if no room, currentFloor if level field empty, else parsed
                String level;
                if (room.isEmpty()) {
                    level = null; // building-only marker, no level needed
                } else if (levelStr.isEmpty()) {
                    level = currentFloor == -2 ? null : String.valueOf(currentFloor);
                } else {
                    level = levelStr;
                }

                Annotation newAnnotation = new Annotation(
                        0L,
                        null,
                        name,
                        room,    // roomName
                        level,
                        latLng.latitude,
                        latLng.longitude,
                        user.getId()
                );

                repository.create(newAnnotation, created -> {
                    if (created != null) {
                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(latLng)
                                .title(created.getBuilding())
                                .snippet(created.getRoomName())
                        );
                        if (marker != null) marker.setTag(created.getId());

                        Toast.makeText(getContext(), "Saved to database", Toast.LENGTH_SHORT).show();
                        fetchAnnotations();
                    } else {
                        Log.e("MAP_ADMIN", "Create failed: " + name);
                        Toast.makeText(getContext(), "Save failed", Toast.LENGTH_SHORT).show();
                    }
                    return null;
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * Returns whether the currently authenticated user has the {@link Role#ADMIN} role.
     *
     * @return {@code true} if the user is non-null and has admin privileges,
     *         {@code false} otherwise
     */
    private boolean isAdmin() {
        return user != null && user.getRole() == Role.ADMIN;
    }
}