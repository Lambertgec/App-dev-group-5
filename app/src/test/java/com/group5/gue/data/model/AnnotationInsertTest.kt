package com.group5.gue.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class AnnotationInsertTest {

    @Test
    fun testAnnotationInsertConstructor() {
        val insert = AnnotationInsert(
            building = "Atlas",
            roomName = "Atlas 0.01",
            level = "0",
            latitude = 51.448,
            longitude = 5.487,
            creatorId = "admin1"
        )

        assertEquals("Atlas", insert.building)
        assertEquals("Atlas 0.01", insert.roomName)
        assertEquals("0", insert.level)
        assertEquals(51.448, insert.latitude!!, 0.001)
        assertEquals(5.487, insert.longitude!!, 0.001)
        assertEquals("admin1", insert.creatorId)
    }

    @Test
    fun testDefaultValues() {
        val insert = AnnotationInsert()
        assertEquals(null, insert.building)
        assertEquals(null, insert.roomName)
        assertEquals(null, insert.level)
        assertEquals(null, insert.latitude)
        assertEquals(null, insert.longitude)
        assertEquals(null, insert.creatorId)
    }

    @Test
    fun testAnnotationInsertEquality() {
        val insert1 = AnnotationInsert(building = "Atlas", roomName = "0.01")
        val insert2 = AnnotationInsert(building = "Atlas", roomName = "0.01")
        val insert3 = AnnotationInsert(building = "Flux", roomName = "0.01")

        assertEquals(insert1, insert2)
        assertEquals(insert1.hashCode(), insert2.hashCode())
        assertNotEquals(insert1, insert3)
    }

    @Test
    fun testAnnotationInsertCopy() {
        val insert = AnnotationInsert(building = "Atlas", roomName = "0.01")
        val copied = insert.copy(building = "Flux")

        assertEquals("Flux", copied.building)
        assertEquals("0.01", copied.roomName)
    }
}
