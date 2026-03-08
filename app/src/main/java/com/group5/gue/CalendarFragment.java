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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private static final Logger log = LoggerFactory.getLogger(CalendarFragment.class);
    private long daySelection = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);

    private CalendarHandler calendarHandler = null;

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

        View datePicker = v.findViewById(R.id.SectionDatePicker);
        datePicker.setVisibility(View.INVISIBLE);

        Spinner spinner = v.findViewById(R.id.calendarPicker);

//        fetch calendars
        calendarHandler = new CalendarHandler(requireActivity());
        ArrayList<String> cals = calendarHandler.getCalendars();

//        populate spinner with users calendars
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cals);
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);


        v.findViewById(R.id.calendarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCalendar();
            }
        });

        v.findViewById(R.id.previousDayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayPrev();
            }
        });

        v.findViewById(R.id.nextDayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayNext();
            }
        });

        v.findViewById(R.id.dayText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTime();
            }
        });
        return v;
    }

    private void setCalendar() {

        Spinner spinner = getActivity().findViewById(R.id.calendarPicker);
        calendarHandler.setCalendar(spinner.getSelectedItem().toString());

        ArrayList<Event> events = calendarHandler.getAllEvents();
        Log.d("calendar", "onClick: ");
        populateView(events);
        TextView dayText = getActivity().findViewById(R.id.dayText);
        dayText.setText("all time");

        View datePicker = getActivity().findViewById(R.id.SectionDatePicker);
        datePicker.setVisibility(View.VISIBLE);

    }

    private void dayNext() {
        daySelection += 86400000;
        populateView(calendarHandler.getDay(daySelection));
    }

    private void dayPrev() {
        daySelection -= 86400000;
        populateView(calendarHandler.getDay(daySelection));
    }

    private void resetTime() {
        daySelection = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);
        populateView(calendarHandler.getAllEvents());
        TextView dayDisplay = getActivity().findViewById(R.id.dayText);
        dayDisplay.setText("all time");
    }

    private void populateView(ArrayList<Event> events) {

        TextView dayDisplay = getActivity().findViewById(R.id.dayText);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM");
        dayDisplay.setText(dateFormat.format(daySelection));


        TextView text = getActivity().findViewById(R.id.textView);
        text.setText("");
        if (events.isEmpty()) {
            text.append("none");
        } else {
            for (Event entry : events) {
                text.append(entry.toString()+"\n");
            }
        }

    }

}