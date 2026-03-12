package com.group5.gue;

import android.Manifest;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.content.Intent;

import com.group5.gue.data.PermissionHandler;
import com.group5.gue.data.auth.AuthManager;
import com.group5.gue.ui.login.LoginActivity;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        Button showCalendarButton = v.findViewById(R.id.showCalendarButton);
        showCalendarButton.setVisibility(View.INVISIBLE);
        return  v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button logoutButton = view.findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            AuthManager.Companion.getInstance(requireContext()).logout(result -> {
                // Navigate back to login
                startActivity(new Intent(requireContext(), LoginActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            });
        });

        Button collectiblesButton = view.findViewById(R.id.collectiblesButton);
        //collectiblesButton.setVisibility(View.VISIBLE);
        FragmentTransaction tr = getChildFragmentManager().beginTransaction();
        //tr.replace(R.id.full_view, new CollectFragment()).commit();
        Fragment collectFragment = new CollectFragment();
        tr.add(R.id.full_view, collectFragment).hide(collectFragment).commit();

        collectiblesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                //transaction.replace(R.id.full_view, new CollectFragment()).commit();
                //transaction.add(R.id.full_view, new CollectFragment()).commit();
                transaction.show(collectFragment).commit();

            }
        });
//        add calendar if has permission, else request it
        PermissionHandler permissionHandler = new PermissionHandler(requireActivity());
        if (permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR)) {
            //        insert calendar fragment
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.frameLayout, new CalendarFragment()).commit();
        } else {
            Button showCalendarButton = view.findViewById(R.id.showCalendarButton);
            showCalendarButton.setVisibility(View.VISIBLE);

            showCalendarButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    permissionHandler.requestPermission(Manifest.permission.READ_CALENDAR);

                    if (permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR)) {
                        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
                        transaction.replace(R.id.frameLayout, new CalendarFragment()).commit();
                        showCalendarButton.setVisibility(View.INVISIBLE);

                    }
                }
            });
        }


    }

}