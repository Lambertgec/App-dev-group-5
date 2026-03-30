package com.group5.gue.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class FollowTest {

    @Test
    fun testFollowModel() {
        val f = Follow(
            userID = "u1",
            followerID = "f1",
            createdAt = "2024-01-01"
        )
        
        assertEquals("u1", f.userID)
        assertEquals("f1", f.followerID)
        assertEquals("2024-01-01", f.createdAt)

        val f2 = f.copy(userID = "u2")
        assertEquals("u2", f2.userID)
        assertNotEquals(f, f2)
        
        val f3 = Follow("u1", "f1", "2024-01-01")
        assertEquals(f, f3)
        assertEquals(f.hashCode(), f3.hashCode())
        assertNotNull(f.toString())
    }
}
