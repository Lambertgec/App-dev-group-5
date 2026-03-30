package com.group5.gue.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class AnnotationTest {

    @Test
    fun testAnnotationModel() {
        val a = Annotation(
            id = 1L,
            createdAt = "now",
            building = "B",
            roomName = "R",
            level = "1",
            latitude = 1.0,
            longitude = 2.0,
            creatorId = "C"
        )
        
        assertEquals(1L, a.id)
        assertEquals("now", a.createdAt)
        assertEquals("B", a.building)
        assertEquals("R", a.roomName)
        assertEquals("1", a.level)
        assertEquals(1.0, a.latitude)
        assertEquals(2.0, a.longitude)
        assertEquals("C", a.creatorId)

        val a2 = a.copy(id = 2L)
        assertEquals(2L, a2.id)
        assertNotEquals(a, a2)
        
        val a3 = Annotation(1L, "now", "B", "R", "1", 1.0, 2.0, "C")
        assertEquals(a, a3)
        assertEquals(a.hashCode(), a3.hashCode())
        assertNotNull(a.toString())
    }

    @Test
    fun testAnnotationInsertModel() {
        val ai = AnnotationInsert(
            building = "B",
            roomName = "R",
            level = "1",
            latitude = 1.0,
            longitude = 2.0,
            creatorId = "C"
        )
        
        assertEquals("B", ai.building)
        assertEquals("R", ai.roomName)
        assertEquals("1", ai.level)
        assertEquals(1.0, ai.latitude)
        assertEquals(2.0, ai.longitude)
        assertEquals("C", ai.creatorId)

        val ai2 = ai.copy(building = "B2")
        assertNotEquals(ai, ai2)
        
        val ai3 = AnnotationInsert("B", "R", "1", 1.0, 2.0, "C")
        assertEquals(ai, ai3)
        assertEquals(ai.hashCode(), ai3.hashCode())
        assertNotNull(ai.toString())
    }
}
