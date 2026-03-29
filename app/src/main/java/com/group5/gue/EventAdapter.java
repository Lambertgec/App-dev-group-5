package com.group5.gue;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter class for displaying a list of events and date headers in a RecyclerView.
 * This adapter supports two types of items: daily headers and individual event tiles.
 */
public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Internal list of items, containing both header strings and event data.
    private List<CalendarItem> items = new ArrayList<>();

    /**
     * Updates the adapter for a single-day view by wrapping events into CalendarItems.
     * 
     * @param events The list of Event objects to display for a specific day.
     */
    public void setEvents(List<Event> events) {
        this.items = new ArrayList<>();
        for (Event e : events) {
            this.items.add(new CalendarItem(e));
        }
        notifyDataSetChanged();
    }

    /**
     * Directly sets the grouped list of items, which typically includes pre-injected headers.
     * 
     * @param items The list of CalendarItem objects (headers and events combined).
     */
    public void setItems(List<CalendarItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    /**
     * Returns whether the item at a given position is a header or an event tile.
     * 
     * @param position Index in the items list.
     * @return Either CalendarItem.TYPE_HEADER or CalendarItem.TYPE_EVENT.
     */
    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    /**
     * Inflates the appropriate layout for the item type.
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == CalendarItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_event_header, parent, false);
            return new HeaderViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    /**
     * Binds data from the item list to the visual components of the ViewHolder.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(items.get(position).header);
        } else if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bind(items.get(position).event);
        }
    }

    /**
     * Returns the total number of items in the list (headers + events).
     */
    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * ViewHolder representing a date header (e.g., "Monday, May 15").
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }

        // Binds the header text to the view.
        public void bind(String text) {
            headerText.setText(text);
        }
    }

    /**
     * ViewHolder representing an individual event card with title, time, and location.
     */
    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView timeTextView;
        private final TextView locationTextView;
        // Formatter for displaying start and end times
        private final SimpleDateFormat timeFormat =
                new SimpleDateFormat("h:mm a", Locale.getDefault());
        // Decorative side bar indicating the event status (ongoing vs upcoming).
        private final View colorBar;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitle);
            timeTextView = itemView.findViewById(R.id.eventTime);
            locationTextView = itemView.findViewById(R.id.eventLocation);
            colorBar = itemView.findViewById(R.id.eventColorBar);
        }

        /**
         * Populates the event card UI and updates the status color bar based on the current time.
         * 
         * @param event The event data to bind.
         */
        public void bind(Event event) {
            titleTextView.setText(event.title != null ? event.title : "No Title");

            String startTimeStr = timeFormat.format(new Date(event.startTime));
            String endTimeStr = timeFormat.format(new Date(event.endTime));
            timeTextView.setText(String.format("%s - %s", startTimeStr, endTimeStr));

            locationTextView.setText(event.location != null ? event.location : "No Location");
            locationTextView.setVisibility(
                    event.location != null && !event.location.isEmpty()
                            ? View.VISIBLE : View.GONE);

            long now = System.currentTimeMillis();
            long timeUntilStart = event.startTime - now;

            // Highlight ongoing events in green and events starting soon in orange
            if (event.startTime <= now && event.endTime >= now) {
                colorBar.setBackgroundColor(0xFF2E7D32); // green — ongoing
            } else if (timeUntilStart > 0 && timeUntilStart <= 60 * 60 * 1000) {
                colorBar.setBackgroundColor(0xFFE65100); // orange — starting within 1 hour
            } else {
                colorBar.setBackgroundColor(0x00000000); // transparent
            }
        }
    }
}