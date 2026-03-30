package com.group5.gue;

import static org.junit.Assert.assertNotNull;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AdminMainActivityTest {

    @Test
    public void testActivityLaunch() {
        try (ActivityScenario<AdminMainActivity> scenario = ActivityScenario.launch(AdminMainActivity.class)) {
            scenario.moveToState(Lifecycle.State.RESUMED);
            scenario.onActivity(activity -> {
                assertNotNull(activity);
                assertNotNull(activity.getSupportFragmentManager().findFragmentById(R.id.admin_frame));
            });
        }
    }
}
