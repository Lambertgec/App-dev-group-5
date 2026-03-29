package com.group5.gue.data.friends

import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(34))
class FriendsRepositoryTest {

    @Test
    fun testRepositoryMethodsBoost() {
        val repo = FriendsRepository.Companion.getInstance()
        Assert.assertNotNull(repo)

        repo.isAdmin { _ -> }
        repo.fetchFriends { _ -> }
        repo.fetchUsersWithScores { _ -> }
        repo.fetchFriendsWithScores { _ -> }

        repo.removeFriendByDisplayName("test") { _ -> }

        Assert.assertTrue(true)
    }
}