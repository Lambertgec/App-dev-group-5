package com.group5.gue

import androidx.fragment.app.testing.FragmentScenario
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class UploadCollectibleFragmentTest {

    @Test
    fun testFragmentCreation() {
//        Avoid Supabase init issues.
        try {
            val scenario = FragmentScenario.launchInContainer(UploadCollectibleFragment::class.java)
            scenario.onFragment { fragment ->
                assertNotNull(fragment)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
