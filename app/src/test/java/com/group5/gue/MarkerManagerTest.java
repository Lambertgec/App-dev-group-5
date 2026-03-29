package com.group5.gue;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MarkerManagerTest {
    @Test
    public void testParseLocationLogic() {
        String[] parts = MarkerManager.parseLocation("Atlas 1.100");
        assertEquals("Atlas", parts[0]);
        assertEquals("1.100", parts[1]);

        String[] partsSingle = MarkerManager.parseLocation("MetaForum");
        assertEquals("MetaForum", partsSingle[0]);
    }
}