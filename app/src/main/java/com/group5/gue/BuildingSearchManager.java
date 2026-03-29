package com.group5.gue;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.group5.gue.data.model.Annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages the building autocomplete search bar.
 * When a building is selected, animates the camera to it and opens its info window.
 * This class handles filtering of annotations to provide relevant search suggestions.
 */
public class BuildingSearchManager {

    /**
     * Interface to communicate building selection events back to the fragment.
     */
    public interface FloorResetListener {
        /** Called when the user selects a building so the caller can reset to the buildings floor. */
        void onBuildingSelected();
    }

    // The UI component for inputting search queries.
    private final AutoCompleteTextView searchView;
    // Listener used to notify the fragment when a building is selected.
    private final FloorResetListener floorResetListener;

    // The live Google Map instance.
    private GoogleMap mMap;
    // The complete list of annotations available for searching.
    private List<Annotation> annotationList;
    // The list of currently rendered markers on the map.
    private List<Marker> activeMarkers;
    // List of unique building names extracted from annotations for suggestions.
    private final List<String> buildingNames = new ArrayList<>();

    /**
     * Constructs a new BuildingSearchManager.
     * 
     * @param searchView         the AutoCompleteTextView in the fragment layout
     * @param floorResetListener called so the fragment can reset floor to buildings view
     */
    public BuildingSearchManager(AutoCompleteTextView searchView,
                                 FloorResetListener floorResetListener) {
        this.searchView = searchView;
        this.floorResetListener = floorResetListener;
    }

    /** 
     * Attaches the map and markers to the manager. Call once the map is ready.
     * 
     * @param map The Google Map instance.
     * @param activeMarkers The current active markers list.
     */
    public void attachMap(GoogleMap map, List<Marker> activeMarkers) {
        this.mMap = map;
        this.activeMarkers = activeMarkers;
    }

    /**
     * Refreshes suggestions from the latest annotation list.
     * Only top-level building entries (level == null) are used as suggestions.
     * 
     * @param annotations The list of annotation objects.
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotationList = annotations;

        buildingNames.clear();
        for (Annotation a : annotations) {
            // Filter only for unique building names at the ground level (building-wide view)
            if (a.getLevel() == null && a.getBuilding() != null
                    && !buildingNames.contains(a.getBuilding())) {
                buildingNames.add(a.getBuilding());
            }
        }

        rebuildAdapter();
    }

    // -------------------------------------------------------------------------

    /**
     * Rebuilds the search adapter and sets up the item click listener for navigation.
     */
    private void rebuildAdapter() {
        Context context = searchView.getContext();
        if (context == null || buildingNames.isEmpty()) return;

        // Use a simple dropdown layout for building suggestions
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_dropdown_item_1line,
                buildingNames);
        searchView.setAdapter(adapter);

        searchView.setOnItemClickListener((parent, view, position, id) -> {
            String selected = (String) parent.getItemAtPosition(position);
            searchView.setText("");
            searchView.clearFocus();
            hideKeyboard();

            if (annotationList == null) return;

            // Find the matching annotation and navigate the map camera
            for (Annotation annotation : annotationList) {
                if (selected.equalsIgnoreCase(annotation.getBuilding())
                        && annotation.getLevel() == null) {

                    LatLng pos = new LatLng(annotation.getLatitude(), annotation.getLongitude());

                    // Tell the fragment to reset to buildings floor before animating
                    floorResetListener.onBuildingSelected();

                    mMap.animateCamera(
                            CameraUpdateFactory.newLatLngZoom(pos, 18f),
                            new GoogleMap.CancelableCallback() {
                                @Override
                                public void onFinish() {
                                    if (mMap == null || activeMarkers == null) return;
                                    // Automatically show the info window for the selected building
                                    for (Marker m : activeMarkers) {
                                        if (selected.equalsIgnoreCase(m.getTitle())) {
                                            m.showInfoWindow();
                                            break;
                                        }
                                    }
                                }
                                @Override public void onCancel() {}
                            });
                    break;
                }
            }
        });
    }

    /**
     * Hides the software keyboard after a selection is made.
     */
    private void hideKeyboard() {
        android.view.inputmethod.InputMethodManager imm =
                (android.view.inputmethod.InputMethodManager)
                        searchView.getContext()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchView.getWindowToken(), 0);
        }
    }
}