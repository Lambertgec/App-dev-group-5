package com.group5.gue;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import com.group5.gue.data.user.UserRepository;
import com.group5.gue.data.model.User;
import com.group5.gue.data.model.Role;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass for displaying the TU/e map with markers.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
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

    UserRepository userRepository = UserRepository.Companion.getInstance();
    User user;


    public MapFragment() {
        user = userRepository.getCachedUser();
        // Required empty public constructor
    }

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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
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
        if (user != null && user.getRole() == Role.USER) {
            // TODO: change to admin
            addAnnotationBtn.setVisibility(View.VISIBLE);
            addAnnotationBtn.setOnClickListener(v -> {
                isAddingMarker = true;
                // Use a Snackbar that stays until dismissed
                instructionSnackbar = Snackbar.make(view, "Tap on the map to place a marker", Snackbar.LENGTH_INDEFINITE);
                
                // Position Snackbar at the top
                View snackbarView = instructionSnackbar.getView();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
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

        updateFloorLevel();

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
                updateFloorLevel();
            }
        });

        btnFloorDown.setOnClickListener(v -> {
            if (currentFloor > -2) {
                currentFloor--;
                updateFloorLevel();
            }
        });
    }

    /**
     * Updates the building markers displayed depending on the current floor level
     */
    private void updateFloorLevel() {
        if (currentFloor == -2) {
            tvFloorLevel.setText("Buildings");
        } else tvFloorLevel.setText("Level " + currentFloor);

        // Update markers for this floor
        showMarkersForFloor(currentFloor);

        // Enable/disable buttons depending on floor
        btnFloorUp.setEnabled(currentFloor < 8);
        btnFloorDown.setEnabled(currentFloor > -2);

        // Gray out disabled buttons
        btnFloorUp.setAlpha(currentFloor < 8 ? 1f : 0.5f);
        btnFloorDown.setAlpha(currentFloor > -2 ? 1f : 0.5f);
    }

    private void showMarkersForFloor(int floor) {
        if (mMap == null || annotationList == null) return;

        mMap.clear(); // remove previous markers

        for (Annotation annotation : annotationList) {
            String levelStr = annotation.getLevel();

            // Case 1: buildings (no level)
            if ((levelStr == null) && floor == -2) {
                LatLng pos = new LatLng(annotation.getLatitude(), annotation.getLongitude());
                mMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .title(annotation.getBuilding())
                        .snippet(annotation.getRoomName())
                        .icon(BitmapDescriptorFactory.defaultMarker(
                                BitmapDescriptorFactory.HUE_BLUE
                        )));
                continue;
            }

            // Case 2: rooms (level is numeric)
            if (levelStr != null) {
                try {
                    int annotationLevel = Integer.parseInt(levelStr);
                    if (annotationLevel == floor && floor > -2) {
                        LatLng pos
                                = new LatLng(annotation.getLatitude(), annotation.getLongitude());
                        mMap.addMarker(new MarkerOptions()
                                .position(pos)
                                .title(annotation.getBuilding())
                                .snippet(annotation.getRoomName())
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        BitmapDescriptorFactory.HUE_BLUE
                                )));
                    }
                } catch (NumberFormatException e) {
                    // Skip rows with invalid level data
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *
     */
    private void fetchAnnotations() {
        repository.getAll(new kotlin.jvm.functions.Function1<List<Annotation>, kotlin.Unit>() {
            @Override
            public kotlin.Unit invoke(List<Annotation> annotations) {
                annotationList = annotations;

                // Only update markers if map is ready
                if (mapReady) {
                    updateFloorLevel();
                }

                return kotlin.Unit.INSTANCE;
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

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tueCampus, 16f));
                // Add markers if annotations are already loaded
        if (annotationList != null && !annotationList.isEmpty()) {
            updateFloorLevel();
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
                if (user != null && user.getRole() == Role.USER) {
                    // TODO: change to admin
                    deleteIcon.setVisibility(View.VISIBLE);
                } else {
                    deleteIcon.setVisibility(View.GONE);
                }

                return window;
            }
        });

        // Adding listeners for Admin actions
        setupAdminListeners();

        // Adding markers to the Map
        loadMarkers();
    }

    private void setupAdminListeners() {
        mMap.setOnMapClickListener(latLng -> {
            if (isAddingMarker) {
                // Dismiss the instruction when location is chosen
                if (instructionSnackbar != null) {
                    instructionSnackbar.dismiss();
                }
                showAddMarkerDialog(latLng);
                isAddingMarker = false;
            }
        });

        // Delete logic: Click a marker's info window to delete it (Admin only)
        mMap.setOnInfoWindowClickListener(marker -> {
            if (user != null && user.getRole() == Role.USER) {
                //TODO: change admin
                new AlertDialog.Builder(getContext())
                        .setTitle("Delete Location")
                        .setMessage("Are you sure you want to delete " + marker.getTitle() + "?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            // TODO: Call Supabase delete logic here
                            marker.remove();
                            Toast.makeText(getContext(), "Marker removed", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });
    }

    private void showAddMarkerDialog(LatLng latLng) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add New Location");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_marker, (ViewGroup) getView(), false);
        final EditText inputName = viewInflated.findViewById(R.id.input_building_name);
        final EditText inputSnippet = viewInflated.findViewById(R.id.input_building_snippet);

        builder.setView(viewInflated);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String name = inputName.getText().toString();
            String snippet = inputSnippet.getText().toString();
            
            if (!name.isEmpty()) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(name)
                        .snippet(snippet)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                
                // TODO: Save to Supabase
                Log.d("MAP_ADMIN", "Saving " + name + " at " + latLng.toString());
                Toast.makeText(getContext(), name + " added successfully", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void loadMarkers() {
        // These are your default markers
        // TODO: get the exact coordinates from database + additional info
        // TODO: add more buildings
        LatLng metaforum = new LatLng(51.447868, 5.487455);
        LatLng atlas = new LatLng(51.44784, 5.48605);
        LatLng auditorium = new LatLng(51.447910, 5.484945);

        mMap.addMarker(new MarkerOptions()
                .position(metaforum)
                .title("MF (Metaforum)")
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE))
                .snippet("Contains the TU/e library and ESA desk"));
        mMap.addMarker(new MarkerOptions()
                .position(atlas)
                .title("Atlas")
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE)));
        mMap.addMarker(new MarkerOptions()
                .position(auditorium)
                .title("Aud (Auditorium)")
                .icon(BitmapDescriptorFactory.defaultMarker(
                        BitmapDescriptorFactory.HUE_BLUE))
                .snippet("Contains most lecture rooms"));
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
            Event e = ongoingEvents.get(0);
            eventBar.setText(getString(R.string.event_happening, e.getLocation()));
            return;
        }

        // Checks for events happening in 1 hour
        ArrayList<Event> upcoming = calendarHandler.getStartingSoon();

        if (!upcoming.isEmpty()) {
            Event e = upcoming.get(0);
            eventBar.setText(getString(R.string.event_starting_soon, e.getLocation()));
        } else {
            eventBar.setText(R.string.no_event_soon);
        }
    }

    // TODO: add building marking for the lecture happening soon.
}
