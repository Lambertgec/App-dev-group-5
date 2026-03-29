package com.group5.gue.data.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class RoleTest {

    @Test
    public void roleEnumValues() {
        assertEquals(Role.USER, Role.valueOf("USER"));
        assertEquals(Role.ADMIN, Role.valueOf("ADMIN"));
    }

    @Test
    public void roleEnumCount() {
        assertEquals(2, Role.values().length);
    }
}
