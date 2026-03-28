package com.group5.gue;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.group5.gue.notifications.NotificationScheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Fragment that displays a calendar interface allowing the user to view events.
 * It supports selecting a calendar, navigating between days, and grouping events by day.
 * Events are displayed in a {@link RecyclerView} with headers for each day.
 */
public class CalendarFragment extends Fragment {

    private static final Logger log = LoggerFactory.getLogger(CalendarFragment.class);

    /** Currently selected day in milliseconds (normalized to start of day). */
    private long daySelection = System.currentTimeMillis() -
            (System.currentTimeMillis() % 86400000);

    /** Handles interactions with the calendar data. */
    private CalendarHandler calendarHandler = null;

    /** Adapter for displaying events in a RecyclerView. */
    private EventAdapter eventAdapter;

    /** RecyclerView showing the events. */
    private RecyclerView recyclerView;

    /** TextView displaying the currently selected day. */
    private TextView dayDisplay;

    /**
     * Required empty public constructor.
     */
    public CalendarFragment() {}

    /**
     * Factory method to create a new instance of this fragment.
     *
     * @return A new instance of CalendarFragment.
     */
    public static CalendarFragment newInstance() {
        return new CalendarFragment();
    }

    /**
     * Inflates the fragment layout.
     *
     * @param inflater LayoutInflater to inflate views.
     * @param container Parent view group.
     * @param savedInstanceState Bundle with saved state.
     * @return The root view of the fragment.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calendar, container, false);
    }

    /**
     * Initializes views, adapters, and event listeners.
     *
     * @param view The fragment's root view.
     * @param savedInstanceState Bundle with saved state.
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize and hide date picker initially
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

        cals.add(0, getString(R.string.choose_calendar));

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item,
                        cals);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        view.findViewById(R.id.calendarButton).setOnClickListener(v -> setCalendar(spinner));
        view.findViewById(R.id.previousDayButton).setOnClickListener(v -> dayPrev());
        view.findViewById(R.id.nextDayButton).setOnClickListener(v -> dayNext());
        dayDisplay.setOnClickListener(v -> resetTime());

        // Restore saved calendar if available
        String savedCalendar = requireContext().getSharedPreferences("app_prefs",
                        Context.MODE_PRIVATE)
                .getString("selected_calendar", null);
        if (savedCalendar != null && cals.contains(savedCalendar)) {
            spinner.setSelection(cals.indexOf(savedCalendar));
            setCalendar(spinner);
        }
    }

    /**
     * Sets the currently selected calendar, retrieves events, and schedules notifications.
     *
     * @param spinner Spinner containing calendar options.
     */
    private void setCalendar(Spinner spinner) {
        Object selectedItem = spinner.getSelectedItem();
        if (selectedItem == null) return;

        String selectedCalendar = selectedItem.toString();

        if (selectedCalendar.equals(getString(R.string.choose_calendar))) {
            Toast.makeText(requireContext(), R.string.choose_calendar, Toast.LENGTH_SHORT).show();
            return;
        }

        CalendarHandler.selectedCalendar = selectedCalendar;
        calendarHandler.setCalendar(selectedCalendar);

        requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putString("selected_calendar", selectedCalendar)
                .apply();

        ArrayList<Event> events = calendarHandler.getAllEvents();

        // Filter past events
        long now = System.currentTimeMillis();
        events.removeIf(event -> event.getEndTime() < now);

        // Schedule notifications for all upcoming events
        for (Event event : events) {
            NotificationScheduler.scheduleNotification(requireContext(), event);
            NotificationScheduler.scheduleProximityNotification(requireContext(), event);
            NotificationScheduler.scheduleCatchUp(requireContext(), event);
        }

        // Sort by start time and update view
        events.sort((a, b) -> Long.compare(a.getStartTime(), b.getStartTime()));
        populateGroupedView(groupByDay(events), "All events");

        View datePicker = getView().findViewById(R.id.SectionDatePicker);
        if (datePicker != null) {
            datePicker.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Groups a list of events by day, adding a header for each day.
     *
     * @param events List of events to group.
     * @return List of CalendarItems with headers and events.
     */
    private ArrayList<CalendarItem> groupByDay(ArrayList<Event> events) {
        ArrayList<CalendarItem> items = new ArrayList<>();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE dd/MM", Locale.getDefault());
        String lastDay = null;

        for (Event event : events) {
            String day = dayFormat.format(event.getStartTime());
            if (!day.equals(lastDay)) {
                items.add(new CalendarItem(day)); // header
                lastDay = day;
            }
            items.add(new CalendarItem(event));
        }
        return items;
    }

    /**
     * Moves the selected day forward by one day and updates the view.
     */
    private void dayNext() {
        daySelection += 86400000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        populateView(calendarHandler.getDay(daySelection), dateFormat.format(daySelection));
    }

    /**
     * Moves the selected day backward by one day and updates the view.
     */
    private void dayPrev() {
        daySelection -= 86400000;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
        populateView(calendarHandler.getDay(daySelection), dateFormat.format(daySelection));
    }

    /**
     * Resets the day selection to the current day and displays all upcoming events.
     */
    private void resetTime() {
        daySelection = System.currentTimeMillis() - (System.currentTimeMillis() % 86400000);
        ArrayList<Event> events = calendarHandler.getAllEvents();
        long now = System.currentTimeMillis();
        events.removeIf(event -> event.getEndTime() < now);
        events.sort((a, b) -> Long.compare(a.getStartTime(), b.getStartTime()));
        populateGroupedView(groupByDay(events), "All events");
    }

    /**
     * Populates the RecyclerView with grouped CalendarItems and updates the day display.
     *
     * @param items List of CalendarItems to display.
     * @param dateText Text to display at the top (e.g., day or "All events").
     */
    private void populateGroupedView(ArrayList<CalendarItem> items, String dateText) {
        if (dayDisplay != null) {
            dayDisplay.setText(dateText);
        }
        if (eventAdapter != null) {
            eventAdapter.setItems(items);
            recyclerView.post(() -> eventAdapter.notifyDataSetChanged());
        }
    }

    /**
     * Populates the RecyclerView with a list of events and updates the day display.
     *
     * @param events List of events to display.
     * @param dateText Text to display at the top (formatted date).
     */
    private void populateView(ArrayList<Event> events, String dateText) {
        if (dayDisplay != null) {
            dayDisplay.setText(dateText);
        }
        if (eventAdapter != null) {
            eventAdapter.setEvents(events);
            recyclerView.post(() -> eventAdapter.notifyDataSetChanged());
        }
    }
}