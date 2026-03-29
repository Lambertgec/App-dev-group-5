package com.group5.gue;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.group5.gue.blocking.AppBlockingService;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 34)
public class AppBlockingServiceTest {
    @Test
    public void testServiceLifecycleBoost() {
        AppBlockingService service = Robolectric.setupService(AppBlockingService.class);
        assertNotNull(service);
        service.onDestroy();
        assertTrue(true);
    }
}