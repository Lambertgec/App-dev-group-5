package com.group5.gue;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.group5.gue.data.annotation.AnnotationRepository;
import com.group5.gue.data.model.Annotation;
import com.group5.gue.data.model.User;
import com.group5.gue.data.user.UserRepository;

import java.util.List;
import java.util.function.Consumer;

/**
 * Thin coordinator fragment. Initialises the four managers and wires them together.
 * Contains no business logic of its own.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST = 1;
    private static final com.google.android.gms.maps.model.LatLng TUE_CAMPUS =
            new com.google.android.gms.maps.model.LatLng(51.448, 5.489);

    // Managers
    private MarkerManager markerManager;
    private EventBarManager eventBarManager;
    private AdminMapManager adminMapManager;
    private BuildingSearchManager buildingSearchManager;

    // Infrastructure
    private GoogleMap mMap;
    private final AnnotationRepository repository = AnnotationRepository.Companion.getInstance();
    private final UserRepository userRepository = UserRepository.Companion.getInstance();
    private User user;

    public MapFragment() {
        user = userRepository.getCachedUser();
    }

    /* ------------------------------------------------------------------ lifecycle */

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // --- MarkerManager ---
        markerManager = new MarkerManager(
                view.findViewById(R.id.btn_floor_up),
                view.findViewById(R.id.btn_floor_down),
                view.findViewById(R.id.tv_floor_level));

        // --- EventBarManager ---
        CalendarHandler calendarHandler = buildCalendarHandler();
        eventBarManager = new EventBarManager(
                view.findViewById(R.id.event_bar),
                calendarHandler,
                location -> markerManager.markLocation(location));

        // --- AdminMapManager ---
        adminMapManager = new AdminMapManager(
                requireContext(),
                repository,
                user,
                markerManager.getCurrentFloor(),
                this::fetchAnnotations);

        // --- BuildingSearchManager ---
        buildingSearchManager = new BuildingSearchManager(
                view.findViewById(R.id.building_search),
                () -> {
                    // Reset to buildings floor before searching
                    markerManager.updateFloorLevel(null, null);
                });

        // --- FAB (admin only) ---
        FloatingActionButton addAnnotationBtn = view.findViewById(R.id.add_annotation);
        if (adminMapManager.isAdmin()) {
            addAnnotationBtn.setVisibility(View.VISIBLE);
            addAnnotationBtn.setOnClickListener(v -> adminMapManager.beginAddingMarker(view));
        } else {
            addAnnotationBtn.setVisibility(View.GONE);
        }

        // --- Center button ---
        view.<Button>findViewById(R.id.btn_center_tue).setOnClickListener(v -> {
            if (mMap != null) mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(TUE_CAMPUS, 16f));
        });

        // --- Map ---
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        fetchAnnotations();
        eventBarManager.refresh(requireContext());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        mMap.setPadding(0, 250, 0, 0);

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TUE_CAMPUS, 16f));

        // Set custom info window (shows delete icon for admins)
        mMap.setInfoWindowAdapter(buildInfoWindowAdapter());

        markerManager.attachMap(mMap);
        adminMapManager.attachListeners(mMap);
        buildingSearchManager.attachMap(mMap, markerManager.getActiveMarkers());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    /* ------------------------------------------------------------------ helpers */

    /**
     * Determines whether the user's current location is within {@code proximityMeters}
     * of the given building/room. Result is delivered via {@code callback}.
     */
    public void isUserNearLocation(String buildingName, String roomName,
                                   double proximityMeters, Consumer<Boolean> callback) {
        android.content.Context context = getContext();
        if (context == null) {
            callback.accept(false);
            return;
        }
        new ProximityChecker(context)
                .check(buildingName, roomName, proximityMeters,
                        markerManager.getAnnotationList(), callback);
    }

    private void fetchAnnotations() {
        repository.getAll(annotations -> {
            if (!isAdded()) return kotlin.Unit.INSTANCE;
            markerManager.setAnnotations(annotations);
            buildingSearchManager.setAnnotations(annotations);
            return kotlin.Unit.INSTANCE;
        });
    }

    private CalendarHandler buildCalendarHandler() {
        try {
            CalendarHandler handler = new CalendarHandler(requireActivity());
            if (CalendarHandler.selectedCalendar != null) {
                handler.setCalendar(CalendarHandler.selectedCalendar);
            }
            return handler;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private GoogleMap.InfoWindowAdapter buildInfoWindowAdapter() {
        return new GoogleMap.InfoWindowAdapter() {
            @Nullable
            @Override
            public View getInfoContents(@NonNull Marker marker) { return null; }

            @Nullable
            @Override
            public View getInfoWindow(@NonNull Marker marker) {
                View window = getLayoutInflater().inflate(R.layout.custom_info_window, null);
                ((TextView) window.findViewById(R.id.info_window_title)).setText(marker.getTitle());
                ((TextView) window.findViewById(R.id.info_window_snippet)).setText(marker.getSnippet());
                ImageView deleteIcon = window.findViewById(R.id.delete);
                deleteIcon.setVisibility(adminMapManager.isAdmin() ? View.VISIBLE : View.GONE);
                return window;
            }
        };
    }
}