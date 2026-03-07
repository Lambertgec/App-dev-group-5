package com.group5.gue;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CollectFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CollectFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CollectFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CollectFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CollectFragment newInstance(String param1, String param2) {
        CollectFragment fragment = new CollectFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    Button col1;
    TextView bocol1;
    Button col2;
    Button xBut;

    boolean userOwnCollect1 = true; //should instead get data from database
    TextView text1;
    TextView text2;

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
        View rv = inflater.inflate(R.layout.fragment_collect, container, false);

        return  rv;    }
    public void setBlackout(View view) {
        bocol1 = view.findViewById(R.id.bocol1);
        if (userOwnCollect1){
            bocol1.setVisibility(view.INVISIBLE);
        }
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setBlackout(view);
        text1 = view.findViewById(R.id.text1);
        text1.setVisibility(view.INVISIBLE);
        text2 = view.findViewById(R.id.text2);
        text2.setVisibility(view.INVISIBLE);

        xBut = view.findViewById(R.id.xbut);
        xBut.setVisibility(view.INVISIBLE);

        xBut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text1.setVisibility(view.INVISIBLE);
                text2.setVisibility(view.INVISIBLE);
                xBut.setVisibility(view.INVISIBLE);
            }
        });

        col1 = view.findViewById(R.id.col1);

        col1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu1 = new PopupMenu(getActivity().getApplicationContext(), v);
                popupMenu1.inflate(R.menu.popup1);
                popupMenu1.show();
                popupMenu1.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (item.getItemId() == R.id.modify) {
                            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=xvFZjo5PgG0")));
                            return true;
                        } else if (item.getItemId() == R.id.desc){
                            //startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=fC7oUOUEEi4")));
                            if (userOwnCollect1){
                                text1.setVisibility(view.VISIBLE);
                                xBut.setVisibility(view.VISIBLE);
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "not owned", Toast.LENGTH_SHORT);
                                toast.show();
                            }

                            return true;
                        }
                        return false;
                    }
                });
            }
        });

        //this part can be added for every collectible, not sure if theres a better way than to copy paste code for all collectibles.


    }
}