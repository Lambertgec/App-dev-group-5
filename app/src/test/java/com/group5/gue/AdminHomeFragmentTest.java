package com.group5.gue;

import static org.junit.Assert.assertNotNull;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AdminHomeFragmentTest {

    @Test
    public void testFragmentLaunch() {
        try (FragmentScenario<AdminHomeFragment> scenario = FragmentScenario.launchInContainer(AdminHomeFragment.class)) {
            scenario.onFragment(fragment -> {
                assertNotNull(fragment.getView());
            });
        }
    }
}
