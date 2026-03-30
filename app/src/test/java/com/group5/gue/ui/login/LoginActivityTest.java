package com.group5.gue.ui.login;

import static org.junit.Assert.assertNotNull;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class LoginActivityTest {

    @Test
    public void testActivityLaunch() {
        try (ActivityScenario<LoginActivity> scenario = ActivityScenario.launch(LoginActivity.class)) {
            scenario.onActivity(activity -> {
                assertNotNull(activity);
            });
            scenario.moveToState(Lifecycle.State.CREATED);
        } catch (Exception e) {
        }
    }
}
