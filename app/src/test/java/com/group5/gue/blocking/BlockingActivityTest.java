package com.group5.gue.blocking;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.content.Intent;

import com.group5.gue.MainActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class BlockingActivityTest {

    private ActivityController<BlockingActivity> controller;
    private BlockingActivity activity;

    @Before
    public void setUp() {
        controller = Robolectric.buildActivity(BlockingActivity.class);
        activity = controller.setup().get();
    }

    @Test
    public void testActivityCreation() {
        assertNotNull(activity);
    }

    @Test
    public void testBackButton_navigatesToMainActivity() {
        activity.findViewById(com.group5.gue.R.id.backButton).performClick();

        Intent expectedIntent = new Intent(activity, MainActivity.class);
        Intent actualIntent = shadowOf(activity).getNextStartedActivity();

        assertTrue(actualIntent.filterEquals(expectedIntent));
        assertTrue(activity.isFinishing());
    }
}
