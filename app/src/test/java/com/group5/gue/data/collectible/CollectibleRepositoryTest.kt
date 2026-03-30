package com.group5.gue.data.collectible

import com.group5.gue.data.model.Collectible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CollectibleRepositoryTest {

    @Test
    fun testInstance() {
        val repo = CollectibleRepository.getInstance()
        assertNotNull(repo)
        val repo2 = CollectibleRepository.getInstance()
        assertEquals(repo, repo2)
    }

    @Test
    fun testTableName() {
        val repo = CollectibleRepository.getInstance()
        assertEquals("collectible", repo.tableName)
    }

    @Test
    fun testRepositoryMethodsPaths() {
        val repo = CollectibleRepository.getInstance()
        
        repo.getAllCollectibles { _ -> }
        repo.deleteCollectible(1) { _ -> }
        
        assertNotNull(repo)
    }
}
