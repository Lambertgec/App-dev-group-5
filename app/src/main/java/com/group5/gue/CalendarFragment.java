package com.group5.gue;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CalendarFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CalendarFragment extends Fragment {

    private static final Logger log = LoggerFactory.getLogger(CalendarFragment.class);
    private long daySelection = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);

    private CalendarHandler calendarHandler = null;
    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private TextView dayDisplay;

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
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View datePicker = view.findViewById(R.id.SectionDatePicker);
        datePicker.setVisibility(View.INVISIBLE);

        dayDisplay = view.findViewById(R.id.dayText);
        recyclerView = view.findViewById(R.id.eventsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        eventAdapter = new EventAdapter();
        recyclerView.setAdapter(eventAdapter);

        Spinner spinner = view.findViewById(R.id.calendarPicker);

        // fetch calendars
        calendarHandler = new CalendarHandler(requireActivity());
        ArrayList<String> cals = calendarHandler.getCalendars();

        // populate spinner with users calendars
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, cals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        view.findViewById(R.id.calendarButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCalendar(spinner);
            }
        });

        view.findViewById(R.id.previousDayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayPrev();
            }
        });

        view.findViewById(R.id.nextDayButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dayNext();
            }
        });

        dayDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetTime();
            }
        });

        // Load previously selected calendar if available
        String savedCalendar = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .getString("selected_calendar", null);
        if (savedCalendar != null && cals.contains(savedCalendar)) {
            spinner.setSelection(cals.indexOf(savedCalendar));
            setCalendar(spinner);
        }
    }

    private void setCalendar(Spinner spinner) {
        Object selectedItem = spinner.getSelectedItem();
        if (selectedItem == null) return;
        
        String selectedCalendar = selectedItem.toString();

        CalendarHandler.selectedCalendar = selectedCalendar;
        calendarHandler.setCalendar(selectedCalendar);

        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("selected_calendar", selectedCalendar)
                .apply();

        ArrayList<Event> events = calendarHandler.getAllEvents();
        populateView(events, "All events");

        View datePicker = getView().findViewById(R.id.SectionDatePicker);
        datePicker.setVisibility(View.VISIBLE);
    }

    private void dayNext() {
        daySelection += 86400000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        populateView(calendarHandler.getDay(daySelection), dateFormat.format(daySelection));
    }

    private void dayPrev() {
        daySelection -= 86400000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        populateView(calendarHandler.getDay(daySelection), dateFormat.format(daySelection));
    }

    private void resetTime() {
        daySelection = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);
        populateView(calendarHandler.getAllEvents(), "All events");
    }

    private void populateView(ArrayList<Event> events, String dateText) {
        dayDisplay.setText(dateText);
        eventAdapter.setEvents(events);
        
        if (events.isEmpty()) {
            Log.d("CalendarFragment", "No events found");
        }
    }
}
