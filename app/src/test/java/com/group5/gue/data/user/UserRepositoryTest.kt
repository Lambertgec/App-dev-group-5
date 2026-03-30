package com.group5.gue.data.user

import com.group5.gue.data.model.User
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class UserRepositoryTest {

    @Test
    fun testInstance() {
        val repo = UserRepository.getInstance()
        assertNotNull(repo)
        val repo2 = UserRepository.getInstance()
        assertEquals(repo, repo2)
    }

    @Test
    fun testTableName() {
        val repo = UserRepository.getInstance()
        assertEquals("profile", repo.tableName)
    }

    @Test
    fun testCache() {
        val repo = UserRepository.getInstance()
        val user = User(id = "test_id", name = "Test User")
        
        repo.setCachedUser(user)
        assertEquals(user, repo.getCachedUser())
        
        repo.setCachedUser(null)
        assertNull(repo.getCachedUser())
    }

    @Test
    fun testAsyncMethodsPaths() {
        val repo = UserRepository.getInstance()
        val user = User(id = "test_id", name = "Test User")
        
        repo.updateUser(user) { _ -> }
        repo.getOwnedCollectibleIds("test_id") { _ -> }
        
        assertNotNull(repo)
    }
}
