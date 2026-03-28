package com.group5.gue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ParseLocationTest {

    @Test
    public void parseLocation_nullInput_returnsNull() {
        assertNull(MapFragment.parseLocation(null));
    }

    @Test
    public void parseLocation_blankInput_returnsNull() {
        assertNull(MapFragment.parseLocation("   "));
    }

    @Test
    public void parseLocation_emptyString_returnsNull() {
        assertNull(MapFragment.parseLocation(""));
    }

    @Test
    public void parseLocation_buildingOnly_returnsBuildingInIndex0() {
        String[] result = MapFragment.parseLocation("Atlas");
        assertNotNull(result);
        assertEquals("Atlas", result[0]);
    }

    @Test
    public void parseLocation_buildingOnly_hasNoRoom() {
        String[] result = MapFragment.parseLocation("Atlas");
        assertNotNull(result);
        assertEquals(1, result.length);
    }

    @Test
    public void parseLocation_buildingAndRoom_returnsBuildingInIndex0() {
        String[] result = MapFragment.parseLocation("Atlas 1.100");
        assertNotNull(result);
        assertEquals("Atlas", result[0]);
    }

    @Test
    public void parseLocation_buildingAndRoom_returnsRoomInIndex1() {
        String[] result = MapFragment.parseLocation("Atlas 1.100");
        assertNotNull(result);
        assertEquals("1.100", result[1]);
    }

    @Test
    public void parseLocation_doubleSpace_stillParsesCorrectly() {
        String[] result = MapFragment.parseLocation("Atlas  1.100");
        assertNotNull(result);
        assertEquals("Atlas", result[0]);
        assertEquals("1.100", result[1]);
    }

    @Test
    public void parseLocation_leadingAndTrailingWhitespace_isTrimmed() {
        String[] result = MapFragment.parseLocation("  Atlas 1.100  ");
        assertNotNull(result);
        assertEquals("Atlas", result[0]);
        assertEquals("1.100", result[1]);
    }

    @Test
    public void parseLocation_multipleLocations_returnsOnlyFirst() {
        String[] result = MapFragment.parseLocation("Atlas 1.100 Flux 2.200");
        assertNotNull(result);
        assertEquals(2, result.length);
        assertEquals("Atlas", result[0]);
        assertEquals("1.100", result[1]);
    }
}