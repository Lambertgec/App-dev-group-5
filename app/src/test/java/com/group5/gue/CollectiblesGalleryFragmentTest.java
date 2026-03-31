package com.group5.gue;

import static org.junit.Assert.assertNotNull;

import androidx.fragment.app.testing.FragmentScenario;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class CollectiblesGalleryFragmentTest {

    @Test
    public void testFragmentCreation() {
        try (FragmentScenario<CollectiblesGalleryFragment> scenario =
                FragmentScenario.launchInContainer(CollectiblesGalleryFragment.class)) {
            scenario.onFragment(fragment -> {
                assertNotNull(fragment);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
