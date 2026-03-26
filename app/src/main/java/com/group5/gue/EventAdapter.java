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

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();

    public void setEvents(List<Event> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView timeTextView;
        private final TextView locationTextView;
        private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.eventTitle);
            timeTextView = itemView.findViewById(R.id.eventTime);
            locationTextView = itemView.findViewById(R.id.eventLocation);
        }

        public void bind(Event event) {
            titleTextView.setText(event.title != null ? event.title : "No Title");
            
            String startTimeStr = timeFormat.format(new Date(event.startTime));
            String endTimeStr = timeFormat.format(new Date(event.endTime));
            timeTextView.setText(String.format("%s - %s", startTimeStr, endTimeStr));
            
            locationTextView.setText(event.location != null ? event.location : "No Location");
            locationTextView.setVisibility(event.location != null && !event.location.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}