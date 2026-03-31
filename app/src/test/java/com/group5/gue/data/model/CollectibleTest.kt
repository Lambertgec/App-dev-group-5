package com.group5.gue.data.model

import com.group5.gue.data.Result
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CollectibleTest {

    @Test
    fun `valid collectible returns success`() {
        val collectible = Collectible(name = "Test Item", score = 10)
        val result = collectible.validated()
        assertTrue(result is Result.Success)
        assertEquals(collectible, (result as Result.Success).data)
    }

    @Test
    fun `collectible with blank name returns error`() {
        val collectible = Collectible(name = "  ", score = 10)
        val result = collectible.validated()
        assertTrue(result is Result.Error)
        assertEquals("Collectible name is required", (result as Result.Error).error.message)
    }

    @Test
    fun `collectible with negative score returns error`() {
        val collectible = Collectible(name = "Test", score = -1)
        val result = collectible.validated()
        assertTrue(result is Result.Error)
        assertEquals("Score cannot be negative", (result as Result.Error).error.message)
    }
    @Test
    fun `collectible with surrounding spaces in name is valid`() {
        val collectible = Collectible(name = "  Valid Name  ", score = 10)
        val result = collectible.validated()
        assertTrue(result is Result.Success)
    }
    @Test
    fun `collectible default values`() {
        val collectible = Collectible()
        assertEquals(0, collectible.id)
        assertEquals(null, collectible.creatorId)
        assertEquals("", collectible.name)
        assertEquals(0, collectible.score)
        assertEquals(null, collectible.description)
        assertEquals(null, collectible.createdAt)
        assertEquals(null, collectible.imageUrl)
    }
}
