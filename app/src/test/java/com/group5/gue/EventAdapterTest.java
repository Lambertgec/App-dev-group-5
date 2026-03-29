package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class EventAdapterTest {

    private EventAdapter adapter;
    private Context context;

    @Before
    public void setUp() {
        adapter = new EventAdapter();
        context = ApplicationProvider.getApplicationContext();
    }

    @Test
    public void testGetItemCount_Empty() {
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testSetEvents() {
        List<Event> events = new ArrayList<>();
        events.add(new Event());
        events.add(new Event());
        
        adapter.setEvents(events);
        
        assertEquals(2, adapter.getItemCount());
        assertEquals(CalendarItem.TYPE_EVENT, adapter.getItemViewType(0));
    }

    @Test
    public void testSetItems_WithHeader() {
        List<CalendarItem> items = new ArrayList<>();
        items.add(new CalendarItem("Header"));
        items.add(new CalendarItem(new Event()));
        
        adapter.setItems(items);
        
        assertEquals(2, adapter.getItemCount());
        assertEquals(CalendarItem.TYPE_HEADER, adapter.getItemViewType(0));
        assertEquals(CalendarItem.TYPE_EVENT, adapter.getItemViewType(1));
    }

    @Test
    public void testCreateViewHolder() {
        ViewGroup parent = new FrameLayout(context);

        RecyclerView.ViewHolder headerHolder = adapter.onCreateViewHolder(parent, CalendarItem.TYPE_HEADER);
        assertTrue(headerHolder instanceof EventAdapter.HeaderViewHolder);

        RecyclerView.ViewHolder eventHolder = adapter.onCreateViewHolder(parent, CalendarItem.TYPE_EVENT);
        assertTrue(eventHolder instanceof EventAdapter.EventViewHolder);
    }
}
