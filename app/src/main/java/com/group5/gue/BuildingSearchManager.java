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
 */
public class BuildingSearchManager {

    public interface FloorResetListener {
        /** Called when the user selects a building so the caller can reset to the buildings floor. */
        void onBuildingSelected();
    }

    private final AutoCompleteTextView searchView;
    private final FloorResetListener floorResetListener;

    private GoogleMap mMap;
    private List<Annotation> annotationList;
    private List<Marker> activeMarkers;
    private final List<String> buildingNames = new ArrayList<>();

    /**
     * @param searchView         the AutoCompleteTextView in the fragment layout
     * @param floorResetListener called so the fragment can reset floor to buildings view
     */
    public BuildingSearchManager(AutoCompleteTextView searchView,
                                 FloorResetListener floorResetListener) {
        this.searchView = searchView;
        this.floorResetListener = floorResetListener;
    }

    /** Call once the map is ready. */
    public void attachMap(GoogleMap map, List<Marker> activeMarkers) {
        this.mMap = map;
        this.activeMarkers = activeMarkers;
    }

    /**
     * Refreshes suggestions from the latest annotation list.
     * Only top-level building entries (level == null) are used as suggestions.
     */
    public void setAnnotations(List<Annotation> annotations) {
        this.annotationList = annotations;

        buildingNames.clear();
        for (Annotation a : annotations) {
            if (a.getLevel() == null && a.getBuilding() != null
                    && !buildingNames.contains(a.getBuilding())) {
                buildingNames.add(a.getBuilding());
            }
        }

        rebuildAdapter();
    }

    // -------------------------------------------------------------------------

    private void rebuildAdapter() {
        Context context = searchView.getContext();
        if (context == null || buildingNames.isEmpty()) return;

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