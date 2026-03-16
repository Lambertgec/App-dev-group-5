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

public class AdminHomeFragment extends Fragment {

    public AdminHomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button btnAddLectureRoom = view.findViewById(R.id.btn_add_lecture_room);
        Button btnCollectibles = view.findViewById(R.id.btn_collectibles);
        Button btnManage = view.findViewById(R.id.btn_manage);
        Button btnLogout = view.findViewById(R.id.btn_admin_logout);

        btnAddLectureRoom.setOnClickListener(v -> {
            // Switch to Map tab in the parent activity
            if (getActivity() != null) {
                BottomNavigationView nav = getActivity().findViewById(R.id.bottomNavigationView);
                if (nav != null) {
                    nav.setSelectedItemId(R.id.map);
                }
            }
        });

        btnCollectibles.setOnClickListener(v -> {
            // TODO: Switch to CollectiblesFragment
            // ((AdminMainActivity)getActivity()).switchFragment(new CollectiblesFragment());
        });

        btnLogout.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().finish(); // Go back to login
            }
        });
    }
}
