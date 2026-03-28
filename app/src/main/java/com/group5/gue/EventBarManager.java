package com.group5.gue;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Manages the event bar shown at the top of the map.
 * Reads from CalendarHandler and updates the TextView accordingly.
 * Calls back into MarkerManager when the user taps a location link.
 */
public class EventBarManager {

    public interface LocationClickListener {
        void onLocationClicked(String location);
    }

    private final TextView eventBar;
    private final CalendarHandler calendarHandler;
    private final LocationClickListener locationClickListener;

    /**
     * @param eventBar              the TextView used as the event bar
     * @param calendarHandler       may be null if calendar permission was not granted
     * @param locationClickListener called with the location string when user taps it
     */
    public EventBarManager(TextView eventBar,
                           CalendarHandler calendarHandler,
                           LocationClickListener locationClickListener) {
        this.eventBar = eventBar;
        this.calendarHandler = calendarHandler;
        this.locationClickListener = locationClickListener;
    }

    /**
     * Refreshes the event bar text based on ongoing/upcoming calendar events.
     * Safe to call any time after the view is attached.
     */
    public void refresh(android.content.Context context) {
        if (calendarHandler == null) {
            eventBar.setText(context.getString(R.string.calendar_permission_message));
            return;
        }

        ArrayList<Event> ongoingEvents = calendarHandler.getOngoingEvent();
        if (!ongoingEvents.isEmpty()) {
            setClickable(context.getString(R.string.event_happening), ongoingEvents.get(0));
            return;
        }

        ArrayList<Event> upcoming = calendarHandler.getStartingSoon();
        if (!upcoming.isEmpty()) {
            setClickable(context.getString(R.string.event_starting_soon), upcoming.get(0));
        } else {
            eventBar.setText(R.string.no_event_soon);
        }
    }

    /**
     * Sets the text of the event bar with a clickable location segment.
     * <p>
     * The method formats the displayed text using a prefix and the event's location.
     * If a valid location is present, that portion of the text becomes clickable,
     * triggering a callback when tapped. If no location is available, a fallback
     * message is shown instead.
     * <p>
     * The clickable portion is implemented using a {@link ClickableSpan}, and
     * link handling is enabled on the TextView.
     *
     * @param prefix A prefix string to display before the event location (e.g., "Next:" or "Now:").
     * @param e      The {@link Event} containing location information to be displayed
     *               and made clickable.
     */
    private void setClickable(String prefix, Event e) {
        if (e.getLocation() == null || e.getLocation().isEmpty()) {
            eventBar.setText(prefix + " (no location)");
            return;
        }

        String fullText = prefix + " " + e.getLocation();
        Log.d("MAP_DEBUG", "Event location: " + e.getLocation());

        int start = fullText.indexOf(e.getLocation());
        int end = start + e.getLocation().length();

        SpannableString spannable = new SpannableString(fullText);
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                locationClickListener.onLocationClicked(e.getLocation());
            }
        }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        eventBar.setText(spannable);
        eventBar.setMovementMethod(LinkMovementMethod.getInstance());
        eventBar.setHighlightColor(Color.TRANSPARENT);
    }
}