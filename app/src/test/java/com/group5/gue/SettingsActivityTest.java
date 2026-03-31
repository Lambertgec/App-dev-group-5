package com.group5.gue;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
public class SettingsActivityTest {

    @Test
    public void testActivityCreation() {
        ActivityController<SettingsActivity> controller = Robolectric.buildActivity(SettingsActivity.class);
        SettingsActivity activity = controller.setup().get();
        assertNotNull(activity);
    }
}
