package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import com.group5.gue.data.annotation.AnnotationRepository;
import com.group5.gue.data.model.User;

import org.junit.Test;

public class AdminMapManagerTest {

    @Test
    public void testResolveLevel() {
        assertEquals(null, AdminMapManager.resolveLevel("", "1", 0));
        assertEquals("2", AdminMapManager.resolveLevel("Room", "2", 0));
        assertEquals("0", AdminMapManager.resolveLevel("Room", "", 0));
        assertEquals(null, AdminMapManager.resolveLevel("Room", "", -2));
    }

    @Test
    public void testIsAdmin() {
        AdminMapManager.MarkerRefreshListener listener = mock(AdminMapManager.MarkerRefreshListener.class);
        
        User adminUser = new User("1", "Admin", 0, true);
        AdminMapManager adminMgr = new AdminMapManager(null, null, adminUser, 0, listener);
        assertTrue(adminMgr.isAdmin());

        User regularUser = new User("2", "User", 0, false);
        AdminMapManager userMgr = new AdminMapManager(null, null, regularUser, 0, listener);
        assertFalse(userMgr.isAdmin());
        
        AdminMapManager nullUserMgr = new AdminMapManager(null, null, null, 0, listener);
        assertFalse(nullUserMgr.isAdmin());
    }
}
