package com.group5.gue.data.friends

import android.util.Log
import com.group5.gue.api.BaseRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import com.group5.gue.api.delete
import com.group5.gue.api.fetchList
import com.group5.gue.api.insert
import com.group5.gue.api.update
import com.group5.gue.data.model.Follow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data class representing a user profile from the 'profile' table.
 */
@Serializable
data class Profile(
    @SerialName("id") val id: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("score") val score: Long = 0L,
    @SerialName("is_admin") var isAdmin: Boolean = false
)

/**
 * Data class used for decoding join results between 'follow' and 'profile' tables.
 */
@Serializable
data class FollowEntry(
    @SerialName("user_id") val userID: String,
    val profile: Profile? = null
)

/**
 * Repository for managing social connections (following/followers) and user profiles.
 */
open class FriendsRepository protected constructor() : BaseRepository {

    // The database table name for follow relationships.
    override val tableName = "follow"
    // Coroutine scope for repository background tasks.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        @Volatile
        private var instance: FriendsRepository? = null

        /**
         * Returns the singleton instance of FriendsRepository.
         */
        @JvmStatic
        fun getInstance(): FriendsRepository {
            return instance ?: synchronized(this) {
                instance ?: FriendsRepository().also { instance = it }
            }
        }

        /**
         * Sets the instance of FriendsRepository.
         */
        @JvmStatic
        fun setInstance(repository: FriendsRepository?) {
            instance = repository
        }
    }

    /**
     * Checks if the currently logged-in user has administrative privileges.
     * @param callback Function called with true if admin, false otherwise.
     */
    open fun isAdmin(callback: (Boolean) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id
            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(false) }
                return@launch
            }
            try {
                val profile = client.from("profile").select {
                    filter { eq("id", currentUserId) }
                }.decodeSingleOrNull<Profile>()
                withContext(Dispatchers.Main) { callback(profile?.isAdmin ?: false) }
            } catch (e: Exception) {
                Log.e("FriendsRepository", "isAdmin check error", e)
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }

    /**
     * Fetches the display names of all users followed by the current user.
     * @param callback Function called with the list of friend names.
     */
    open fun fetchFriends(callback: (List<String>) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id

            if (currentUserId == null) {
                Log.w("FriendsRepository", "No user session found")
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val friends = try {
                val response = client.from(tableName).select(
                    Columns.raw("user_id, profile!user_id(display_name)")
                ) {
                    filter {
                        eq("follower_id", currentUserId)
                    }
                }

                val entries = response.decodeList<FollowEntry>()
                Log.d("FriendsRepository", "fetchFriends: Found ${entries.size} following for $currentUserId. Data: ${response.data}")
                entries.mapNotNull { it.profile?.displayName }

            } catch (e: Exception) {
                Log.e("FriendsRepository", "fetchFriends: Error decoding or fetching", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                callback(friends)
            }
        }
    }

    /**
     * Fetches the top 10 users ranked by their score for a global leaderboard.
     * @param callback Function called with the list of top profiles.
     */
    open fun fetchUsersWithScores(callback: (List<Profile>) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id

            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val topUsers = try {
                val response = client.from("profile").select {
                    filter {
                        neq("display_name", "")
                    }
                    order(column = "score", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                    limit(10)
                }

                response.decodeList<Profile>()
            } catch (e: Exception) {
                Log.e("FriendsRepository", "fetchUsersWithScores error", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                callback(topUsers)
            }
        }
    }

    /**
     * Fetches the profiles of friends (followed users) including their scores.
     * @param callback Function called with the list of profiles, sorted by score.
     */
    open fun fetchFriendsWithScores(callback: (List<Profile>) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id

            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val friends = try {
                val response = client.from(tableName).select(
                    Columns.raw("user_id, profile!user_id(display_name, score)")
                ) {
                    filter {
                        eq("follower_id", currentUserId)
                    }
                }

                val entries = response.decodeList<FollowEntry>()
                entries.mapNotNull { it.profile }
                    .sortedByDescending { it.score }

            } catch (e: Exception) {
                Log.e("FriendsRepository", "fetchFriendsWithScores error", e)
                emptyList()
            }
            withContext(Dispatchers.Main) {
                callback(friends)
            }
        }
    }

    /**
     * Adds a friend relationship. If current user is admin, it elevates the target user instead.
     * @param displayName The display name of the user to follow or elevate.
     * @param callback Function called with success status and a status message.
     */
    open fun addFriendByDisplayName(displayName: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                val currentUserId = client.auth.currentSessionOrNull()?.user?.id
                if (currentUserId == null) {
                    withContext(Dispatchers.Main) {
                        callback(false, "You must be logged in.")
                    }
                    return@launch
                }

                val currentUserProfile = client.from("profile").select {
                    filter { eq("id", currentUserId) }
                }.decodeSingleOrNull<Profile>()

                if (currentUserProfile?.isAdmin == true) {
                    elevatePrivilege(displayName, callback)
                } else {
                    val profile = client.from("profile").select {
                        filter { eq("display_name", displayName) }
                    }.decodeSingleOrNull<Profile>()

                    if (profile == null || profile.id == null) {
                        withContext(Dispatchers.Main) { callback(false, "User not found") }
                        return@launch
                    }

                    if (profile.id == currentUserId) {
                        withContext(Dispatchers.Main) {
                            callback(false, "You cannot follow yourself.")
                        }
                        return@launch
                    }

                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val newFollow = Follow(
                        userID = profile.id,
                        followerID = currentUserId,
                        createdAt = sdf.format(Date())
                    )

                    val result = insert<Follow, Follow>(newFollow)
                    withContext(Dispatchers.Main) {
                        if (result != null) callback(true, "Followed ${profile.displayName}!")
                        else callback(false, "Failed to update database")
                    }
                }
            }
            catch (e: Exception) {
                Log.e("FriendsRepository", "Add failed", e)
                withContext(Dispatchers.Main) { callback(false, "Error: ${e.message}") }
            }
        }
    }

    /**
     * Internal method to elevate a user's privileges to admin.
     */
    private suspend fun elevatePrivilege(displayName: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                val currentUserId = client.auth.currentSessionOrNull()?.user?.id
                if (currentUserId == null) {
                    withContext(Dispatchers.Main) {callback(false, "You must be logged in.")}
                    return@launch
                }

                val currentUserProfile = client.from("profile").select {
                    filter { eq("id", currentUserId) }
                }.decodeSingleOrNull<Profile>()

                if (currentUserProfile?.isAdmin != true) {
                    withContext(Dispatchers.Main) { callback(false, "Unauthorized: Admin only") }
                    return@launch
                }

                val targetProfile = client.from("profile").select {
                    filter { eq("display_name", displayName) }
                }.decodeSingleOrNull<Profile>()

                if (targetProfile == null || targetProfile.id == null) {
                    withContext(Dispatchers.Main) { callback(false, "User not found") }
                    return@launch
                }

                targetProfile.isAdmin = true

                val profileRepo = object : BaseRepository {override val tableName = "profile"}
                val result = profileRepo.update("id", targetProfile.id, targetProfile)

                withContext(Dispatchers.Main) {
                    if (result != null) callback(true, "elevated privilege for ${targetProfile.displayName}!")
                    else callback(false, "Failed to elevate privilege")
                }

            } catch (e: Exception) {
                Log.e("FriendsRepository", "Elevate failed", e)
                withContext(Dispatchers.Main) { callback(false, "Error: ${e.message}") }
            }
        }
    }

    /**
     * Removes a follow relationship by the target user's display name.
     * @param displayName The name of the user to unfollow.
     * @param callback Function called with true if successful.
     */
    open fun removeFriendByDisplayName(displayName: String, callback: (Boolean) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id
            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(false) }
                return@launch
            }

            try {
                val profile = client.from("profile").select {
                    filter { eq("display_name", displayName) }
                }.decodeSingleOrNull<Profile>()

                if (profile == null || profile.id == null) {
                    withContext(Dispatchers.Main) { callback(false) }
                    return@launch
                }

                client.from(tableName).delete {
                    filter {
                        eq("follower_id", currentUserId)
                        eq("user_id", profile.id)
                    }
                }

                withContext(Dispatchers.Main) { callback(true) }
            } catch (e: Exception) {
                Log.e("FriendsRepository", "Remove failed", e)
                withContext(Dispatchers.Main) { callback(false) }
            }
        }
    }
}
