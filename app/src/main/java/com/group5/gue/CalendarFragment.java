package com.group5.gue;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CalendarFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CalendarFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CalendarFragment newInstance(String param1, String param2) {
        CalendarFragment fragment = new CalendarFragment();
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
        View v = inflater.inflate(R.layout.fragment_calendar, container, false);

        TextView text = v.findViewById(R.id.textView);
        Button button = v.findViewById(R.id.calendarButton);
        Spinner spinner = v.findViewById(R.id.calendarPicker);

//        fetch calendars
        CalendarHandler calendarHandler = null;
        try {
            calendarHandler = new CalendarHandler(requireActivity());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ArrayList<String> cals = calendarHandler.getCalendars();

//        populate spinner with users calendars
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cals);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        CalendarHandler finalCalendarHandler = calendarHandler;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("");

                CalendarHandler.selectedCalendar = spinner.getSelectedItem().toString();

                finalCalendarHandler.setCalendar(spinner.getSelectedItem().toString());

                ArrayList<Event> events = finalCalendarHandler.getOngoingEvent();

                text.append("currently ongoing: \n");
                if (events.size() == 0) {
                    text.append("none");
                } else {
                    for (Event entry : events) {
                        text.append(entry.toString());
                    }

                }

                events = finalCalendarHandler.fetchEvents();
                text.append("\n\n all other events: \n");
                for (Event entry : events) {
                    text.append(entry.toString());
                }

            }
        });
        return v;
    }
}