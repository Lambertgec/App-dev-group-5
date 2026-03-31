package com.group5.gue.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowActivity;

@RunWith(RobolectricTestRunner.class)
public class PermissionHandlerTest {

    private Activity activity;
    private PermissionHandler permissionHandler;

    @Before
    public void setUp() {
        activity = Robolectric.buildActivity(Activity.class).setup().get();
        permissionHandler = new PermissionHandler(activity);
    }

    @Test
    public void testCheckPermission_notGranted() {
        assertFalse(permissionHandler.checkPermission(Manifest.permission.READ_CALENDAR));
    }

    @Test
    public void testRequestPermission() {
        permissionHandler.requestPermission(Manifest.permission.READ_CALENDAR);
        
        ShadowActivity shadowActivity = shadowOf(activity);
        ShadowActivity.PermissionsRequest request = shadowActivity.getLastRequestedPermission();
        
        assertNotNull(request);
        assertEquals(Manifest.permission.READ_CALENDAR, request.requestedPermissions[0]);
    }

    private void assertNotNull(Object obj) {
        if (obj == null) throw new AssertionError("Object is null");
    }

    private void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) throw new AssertionError("Expected " + expected + " but was " + actual);
    }
}
