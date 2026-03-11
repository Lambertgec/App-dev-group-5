package com.group5.gue;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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
import android.widget.TextView;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback {
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST = 1;
    LatLng tueCampus = new LatLng(51.448, 5.489);
    AnnotationRepository repository = AnnotationRepository.Companion.getInstance();
    private List<Annotation> annotationList;

    private int currentFloor = -2; // starting level
    private Button btnFloorUp, btnFloorDown;
    private TextView tvFloorLevel;
    private boolean mapReady = false;


    public MapFragment() {
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
}