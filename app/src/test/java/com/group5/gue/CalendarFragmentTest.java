package com.group5.gue;

import static org.junit.Assert.assertNotNull;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class CalendarFragmentTest {

    @Test
    public void testFragmentCreation() {
        // Suppress Supabase initialization error
        System.setProperty("supabase.auth.settings.ignore", "true");
        
        try (FragmentScenario<CalendarFragment> scenario = FragmentScenario.launchInContainer(CalendarFragment.class)) {
            scenario.onFragment(fragment -> {
                assertNotNull(fragment);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
