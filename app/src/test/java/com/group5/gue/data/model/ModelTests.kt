package com.group5.gue.data.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ModelTests {

    @Test
    fun `Follow data class test`() {
        val follow = Follow(userID = "user1", followerID = "follower1", createdAt = "2023-10-27")
        assertEquals("user1", follow.userID)
        assertEquals("follower1", follow.followerID)
        assertEquals("2023-10-27", follow.createdAt)
    }

    @Test
    fun `Annotation data class test`() {
        val annotation = Annotation(
            id = 1L,
            createdAt = "now",
            building = "Atlas",
            roomName = "1.100",
            level = "1",
            latitude = 51.447,
            longitude = 5.484,
            creatorId = "creator1"
        )
        assertEquals(1L, annotation.id)
        assertEquals("Atlas", annotation.building)
        assertEquals("1.100", annotation.roomName)
        assertEquals(51.447, annotation.latitude!!, 0.001)
    }

    @Test
    fun `AnnotationInsert data class test`() {
        val insert = AnnotationInsert(
            building = "MetaForum",
            roomName = "0.100",
            level = "0",
            latitude = 51.448,
            longitude = 5.485,
            creatorId = "creator2"
        )
        assertEquals("MetaForum", insert.building)
        assertEquals("0.100", insert.roomName)
        assertEquals("creator2", insert.creatorId)
    }
}
