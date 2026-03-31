package com.group5.gue.blocking;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AppBlockingManagerTest {

    private AppBlockingManager manager;
    private Context context;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        manager = new AppBlockingManager(context);
    }

    @Test
    public void testDefaultBlockedApps() {
        // Test that some default apps are blocked
        assertTrue(manager.isAppBlocked("com.android.chrome"));
        assertTrue(manager.isAppBlocked("com.instagram.android"));
        assertFalse(manager.isAppBlocked("com.group5.gue")); // Our own app shouldn't be blocked
    }

    @Test
    public void testAddBlockedApp() {
        String newApp = "com.test.distraction";
        assertFalse(manager.isAppBlocked(newApp));
        
        manager.addBlockedApp(newApp);
        assertTrue(manager.isAppBlocked(newApp));
    }

    @Test
    public void testIsBlockingEnabledDefault() {
        assertFalse(manager.isBlockingEnabled());
    }
}
