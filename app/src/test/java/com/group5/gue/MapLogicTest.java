package com.group5.gue;

import org.junit.Test;
import static org.junit.Assert.*;

public class MapLogicTest {
    @Test
    public void resolveLevel_noRoom_returnsNull() {
        // Building-only marker never has a level
        assertNull(AdminMapManager.resolveLevel("", "", 3));
        assertNull(AdminMapManager.resolveLevel("", "5", 3));
        assertNull(AdminMapManager.resolveLevel("", "", -2));
    }

    @Test
    public void resolveLevel_roomWithExplicitLevel_usesExplicitLevel() {
        assertEquals("2", AdminMapManager.resolveLevel("1.100", "2", 5));
    }

    @Test
    public void resolveLevel_roomNoExplicitLevel_usesCurrentFloor() {
        assertEquals("3", AdminMapManager.resolveLevel("1.100", "", 3));
    }

    @Test
    public void resolveLevel_roomNoExplicitLevel_buildingsView_returnsNull() {
        // currentFloor == -2 means the "buildings" overview — no floor to assign
        assertNull(AdminMapManager.resolveLevel("1.100", "", -2));
    }

    @Test
    public void resolveLevel_roomWithExplicitLevelZero_isNotTreatedAsEmpty() {
        assertEquals("0", AdminMapManager.resolveLevel("1.100", "0", 3));
    }
}