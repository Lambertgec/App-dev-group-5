package com.group5.gue;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
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

import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;


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

        //Displays the map
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager()
                        .findFragmentById(R.id.map);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        btnCenterTue.setOnClickListener(v -> {
            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(tueCampus, 16f));
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        try {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(
                    getContext(), R.raw.map_style));
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }

        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);

        } else {
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tueCampus, 16f));

        // Adding markers to the Map
        // TODO: get the exact coordinates from database + additional info + add more buildings
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
     *
     * The method checks the user's calendar (via CalendarHandler) to determine:
     * 1. If there is an event currently ongoing.
     * 2. If there is an upcoming event starting soon.
     * 3. If there are no relevant events.
     *
     * Based on this information, the text in the event bar is updated to inform
     * the user where their lecture/event is happening or that there are no
     * upcoming events within the checked time window.
     *
     * If the calendar cannot be accessed (e.g., permission not granted or
     * CalendarHandler not initialized), the user is prompted to give calendar
     * permission so the app can show the building of upcoming lectures.
     */
    private void updateEventBar() {
        if (calendarHandler == null) {
            eventBar.setText("Give permission to your calendar to display a building for a lecture starting soon");
            return;
        }

        ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();

        if (ongoingEvents.size() > 0) {
            Event e = ongoingEvents.get(0);
            eventBar.setText("The event is happening at " + e.getLocation());
            return;
        }

        ArrayList<Event> upcoming = calendarHandler.getStartingSoon();

        // debugging code
        Log.d("MAP_DEBUG", "Upcoming events count: " + upcoming.size());
        for (Event e : upcoming) {
            Log.d("MAP_DEBUG", "Event -> " + e.toString());
        }

        // TODO: add check for event happening in an hour
        if (upcoming.size() > 0) {
            Event e = upcoming.get(0);
            eventBar.setText("There is an event starting soon at " + e.getLocation());
        } else {
            eventBar.setText("There is no event in an hour");
        }
    }

    // TODO: add building marking for the lecture happening soon.
}