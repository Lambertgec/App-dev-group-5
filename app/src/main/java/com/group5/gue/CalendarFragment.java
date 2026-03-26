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
import android.widget.Spinner;
import android.widget.TextView;

import com.group5.gue.notifications.NotificationScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class CalendarFragment extends Fragment {

    private static final Logger log = LoggerFactory.getLogger(CalendarFragment.class);
    private long daySelection = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);

    private CalendarHandler calendarHandler = null;
    private EventAdapter eventAdapter;
    private RecyclerView recyclerView;
    private TextView dayDisplay;

    public CalendarFragment() {
        // Required empty public constructor
    }

    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        calendarHandler = new CalendarHandler(requireActivity());
        ArrayList<String> cals = calendarHandler.getCalendars();

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

        // Filter out events that have already started
        long now = System.currentTimeMillis();
        events.removeIf(event -> event.getEndTime() < now);

        // Schedule notifications + catch-up for all events
        for (Event event : events) {
            NotificationScheduler.scheduleNotification(requireContext(), event);
            NotificationScheduler.scheduleProximityNotification(requireContext(), event);
            NotificationScheduler.scheduleCatchUp(requireContext(), event);
        }

        populateView(events, "All events");

        View datePicker = getView().findViewById(R.id.SectionDatePicker);
        if (datePicker != null) {
            datePicker.setVisibility(View.VISIBLE);
        }
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
        ArrayList<Event> events = calendarHandler.getAllEvents();
        long now = System.currentTimeMillis();
        events.removeIf(event -> event.getEndTime() < now);
        populateView(events, "All events");
    }

    private void populateView(ArrayList<Event> events, String dateText) {
        if (dayDisplay != null) {
            dayDisplay.setText(dateText);
        }
        if (eventAdapter != null) {
            eventAdapter.setEvents(events);
            recyclerView.post(() -> eventAdapter.notifyDataSetChanged()); // force remeasure
        }

        if (events.isEmpty()) {
            Log.d("CalendarFragment", "No events found");
        }
    }
}