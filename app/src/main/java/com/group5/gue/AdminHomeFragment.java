package com.group5.gue;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * AdminHomeFragment serves as the main dashboard for administrative users.
 * It provides shortcuts to administrative tasks like adding lecture rooms,
 * managing collectibles, and logging out of the admin session.
 *
 */
public class AdminHomeFragment extends Fragment {

    /**
     * Required empty public constructor for fragment instantiation.
     */
    public AdminHomeFragment() {
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     *
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    /**
     * Called immediately after onCreateView has returned, but before any saved state has been restored in to the view.
     * Sets up click listeners for the admin dashboard buttons.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Finding references to UI buttons in the inflated layout
        Button btnAddLectureRoom = view.findViewById(R.id.btn_add_lecture_room);
        Button btnCollectibles = view.findViewById(R.id.btn_collectibles);
        Button btnManage = view.findViewById(R.id.btn_manage);
        Button btnLogout = view.findViewById(R.id.btn_admin_logout);

        // Logic for 'Add Lecture Room' button, switches to the Map tab
        btnAddLectureRoom.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Access the bottom navigation view from the host activity
                BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigationView);
                if (nav != null) {
                    // Set the selected item to 'map' to trigger fragment switch in parent
                    nav.setSelectedItemId(R.id.map);
                }
            }
        });

        // Logic for 'Collectibles' button, intended to show collectibles management
        btnCollectibles.setOnClickListener(v -> {
            // TODO: Switch to CollectiblesFragment
            // ((AdminMainActivity)getActivity()).switchFragment(new CollectiblesFragment());
        });

        // Logic for 'Logout' button. finishes the host activity and returns to login
        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish(); // Go back to login
            }
        });
    }
}
