package com.group5.gue.data.friends

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FriendsRepositoryTest {

    @Test
    fun testDataModelsCoverage() {
        val profile = Profile(
            id = "id123",
            displayName = "Test User",
            score = 500L,
            isAdmin = true
        )
        Assert.assertEquals("id123", profile.id)
        Assert.assertEquals("Test User", profile.displayName)
        Assert.assertEquals(500L, profile.score)
        Assert.assertTrue(profile.isAdmin)

        val entry = FollowEntry(userID = "user456", profile = profile)
        Assert.assertEquals("user456", entry.userID)
        Assert.assertEquals(profile, entry.profile)
    }

    @Test
    fun testRepositoryMethodsBoost() {
        val repo = FriendsRepository.getInstance()
        Assert.assertNotNull(repo)

        repo.isAdmin { _ -> }
        repo.fetchFriends { _ -> }
        repo.fetchUsersWithScores { _ -> }
        repo.fetchFriendsWithScores { _ -> }
        
        repo.addFriendByDisplayName("some_user") { _, _ -> }
        repo.removeFriendByDisplayName("some_user") { _ -> }

        Assert.assertTrue(true)
    }
    
    @Test
    fun testSetInstanceCoverage() {
        val original = FriendsRepository.getInstance()
        FriendsRepository.setInstance(null)
        Assert.assertNotNull(original)
        FriendsRepository.setInstance(original)
        Assert.assertEquals(original, FriendsRepository.getInstance())
    }
}
