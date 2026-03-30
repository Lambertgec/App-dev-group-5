package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.widget.Button;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class MarkerManagerTest {

    private MarkerManager markerManager;
    private Button btnUp;
    private Button btnDown;
    private TextView tvFloor;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        btnUp = new Button(context);
        btnDown = new Button(context);
        tvFloor = new TextView(context);
        markerManager = new MarkerManager(btnUp, btnDown, tvFloor);
    }

    @Test
    public void testInitialFloor() {
        assertEquals(-2, markerManager.getCurrentFloor());
    }

    @Test
    public void testFloorUp() {
        btnUp.performClick();
        assertEquals(-1, markerManager.getCurrentFloor());
    }

    @Test
    public void testFloorDown() {
        btnUp.performClick(); // to -1
        btnDown.performClick(); // back to -2
        assertEquals(-2, markerManager.getCurrentFloor());
    }

    @Test
    public void testFloorBoundaries() {
        btnDown.performClick();
        assertEquals(-2, markerManager.getCurrentFloor());

        for (int i = 0; i < 20; i++) {
            btnUp.performClick();
        }
        assertEquals(8, markerManager.getCurrentFloor());
    }

    @Test
    public void testFloorTextUpdate() {
        markerManager.updateFloorLevel(null, null);
        assertEquals("Buildings", tvFloor.getText().toString());

        btnUp.performClick(); // to -1
        markerManager.updateFloorLevel(null, null);
        assertEquals("Level -1", tvFloor.getText().toString());
    }
}
