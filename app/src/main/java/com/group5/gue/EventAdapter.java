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

public class EventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CalendarItem> items = new ArrayList<>();

    // For single-day view — wraps events into CalendarItems internally
    public void setEvents(List<Event> events) {
        this.items = new ArrayList<>();
        for (Event e : events) {
            this.items.add(new CalendarItem(e));
        }
        notifyDataSetChanged();
    }

    // For grouped all-events view — headers already injected
    public void setItems(List<CalendarItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

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

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind(items.get(position).header);
        } else if (holder instanceof EventViewHolder) {
            ((EventViewHolder) holder).bind(items.get(position).event);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ---- Header ViewHolder ----

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView headerText;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            headerText = itemView.findViewById(R.id.headerText);
        }

        public void bind(String text) {
            headerText.setText(text);
        }
    }

    // ---- Event ViewHolder — unchanged ----

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView timeTextView;
        private final TextView locationTextView;
        private final SimpleDateFormat timeFormat =
                new SimpleDateFormat("h:mm a", Locale.getDefault());
        private final View colorBar;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitle);
            timeTextView = itemView.findViewById(R.id.eventTime);
            locationTextView = itemView.findViewById(R.id.eventLocation);
            colorBar = itemView.findViewById(R.id.eventColorBar);
        }

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