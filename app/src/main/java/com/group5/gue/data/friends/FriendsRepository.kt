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
 * Data class representing a user profile fetched from the Supabase 'profile' table.
 * 
 * @property id The unique identifier of the user (UUID string).
 * @property displayName The publicly visible name of the user.
 * @property score The current total points accumulated by the user.
 * @property isAdmin Boolean flag indicating if the user has administrator rights.
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
 * This structure matches the nested JSON returned by Supabase when performing a join query.
 * 
 * @property userID The ID of the user being followed.
 * @property profile The profile details of the user being followed, retrieved via join.
 */
@Serializable
data class FollowEntry(
    @SerialName("user_id") val userID: String,
    val profile: Profile? = null
)

/**
 * Repository responsible for managing social relationships and user profile data.
 * It provides abstraction over Supabase database calls for following users, 
 * checking admin status, and fetching leaderboards.
 * 
 * This class uses Kotlin Coroutines for asynchronous operations and handles thread switching.
 */
open class FriendsRepository protected constructor() : BaseRepository {

    // The database table name for follow relationships.
    override val tableName = "follow"
    
    /** 
     * The coroutine scope used for all background operations in this repository.
     * Uses SupervisorJob to ensure failure in one task doesn't cancel others.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        // The singleton instance of the repository.
        @Volatile
        private var instance: FriendsRepository? = null

        /**
         * Returns the thread-safe singleton instance of FriendsRepository.
         * If the instance doesn't exist, it creates one using the protected constructor.
         * 
         * @return The active FriendsRepository instance.
         */
        @JvmStatic
        fun getInstance(): FriendsRepository {
            return instance ?: synchronized(this) {
                instance ?: FriendsRepository().also { instance = it }
            }
        }

        /**
         * Manually sets the repository instance.
         * Primarily used in unit tests to inject mock or fake repositories.
         * 
         * @param repository The repository instance to use.
         */
        @JvmStatic
        fun setInstance(repository: FriendsRepository?) {
            instance = repository
        }
    }

    /**
     * Checks if the currently authenticated user has administrator privileges.
     * Fetches the user profile from the database to check the 'is_admin' field.
     * 
     * @param callback A function called on the Main thread with the result (true if admin).
     */
    open fun isAdmin(callback: (Boolean) -> Unit) {
        scope.launch {
            // Get current session from Supabase Auth
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id
            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(false) }
                return@launch
            }
            try {
                // Fetch only the profile of the current user
                val profile = client.from("profile").select {
                    filter { eq("id", currentUserId) }
                }.decodeSingleOrNull<Profile>()
                
                // Return admin status or false if profile missing
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
                // Perform join query: fetch user_id from follow and display_name from joined profile
                val response = client.from(tableName).select(
                    Columns.raw("user_id, profile!user_id(display_name)")
                ) {
                    filter {
                        eq("follower_id", currentUserId)
                    }
                }

                // Decode the result into FollowEntry objects and extract names
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
     * Retrieves the top 10 users with the highest scores for the global leaderboard.
     * Filters out users without a display name.
     * 
     * @param callback A function called on the Main thread with the list of top 10 Profiles.
     */
    open fun fetchUsersWithScores(callback: (List<Profile>) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id

            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val topUsers = try {
                // Query profiles table, sort by score descending, limit to 10
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
     * Fetches the profiles of users followed by the current user, including their scores.
     * Results are sorted locally by score in descending order for the 'Friends Leaderboard'.
     * 
     * @param callback A function called on the Main thread with the sorted list of Profiles.
     */
    open fun fetchFriendsWithScores(callback: (List<Profile>) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id

            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(emptyList()) }
                return@launch
            }

            val friends = try {
                // Query follows table and join with profile to get names and scores
                val response = client.from(tableName).select(
                    Columns.raw("user_id, profile!user_id(display_name, score)")
                ) {
                    filter {
                        eq("follower_id", currentUserId)
                    }
                }

                // Extract profiles from the wrapped join result and sort them
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
     * Adds a friend by their display name.
     * 
     * Special Behavior:
     * - If the current user is an admin, this method instead elevates the target user to admin.
     * - Otherwise, it creates a new entry in the 'follow' table.
     * 
     * @param displayName The name of the user to follow or elevate.
     * @param callback Callback returning success status and a descriptive UI message.
     */
    open fun addFriendByDisplayName(displayName: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                // Ensure user is logged in
                val currentUserId = client.auth.currentSessionOrNull()?.user?.id
                if (currentUserId == null) {
                    withContext(Dispatchers.Main) {
                        callback(false, "You must be logged in.")
                    }
                    return@launch
                }

                // Check current user's role
                val currentUserProfile = client.from("profile").select {
                    filter { eq("id", currentUserId) }
                }.decodeSingleOrNull<Profile>()

                if (currentUserProfile?.isAdmin == true) {
                    // Admin branch: elevate the user
                    elevatePrivilege(displayName, callback)
                } else {
                    // Standard user branch: follow the user
                    val profile = client.from("profile").select {
                        filter { eq("display_name", displayName) }
                    }.decodeSingleOrNull<Profile>()

                    // Validation: user must exist
                    if (profile == null || profile.id == null) {
                        withContext(Dispatchers.Main) { callback(false, "User not found") }
                        return@launch
                    }

                    // Validation: cannot follow yourself
                    if (profile.id == currentUserId) {
                        withContext(Dispatchers.Main) {
                            callback(false, "You cannot follow yourself.")
                        }
                        return@launch
                    }

                    // Create follow record with current date
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val newFollow = Follow(
                        userID = profile.id,
                        followerID = currentUserId,
                        createdAt = sdf.format(Date())
                    )

                    // Insert into Supabase
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
     * Internal helper method to elevate a user's role to Administrator.
     * Restricted to being called by an admin user.
     * 
     * @param displayName Display name of the user to be promoted.
     * @param callback Callback returning success and status message.
     */
    private suspend fun elevatePrivilege(displayName: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                val currentUserId = client.auth.currentSessionOrNull()?.user?.id
                if (currentUserId == null) {
                    withContext(Dispatchers.Main) {callback(false, "You must be logged in.")}
                    return@launch
                }

                // Verify admin status again before promotion logic
                val currentUserProfile = client.from("profile").select {
                    filter { eq("id", currentUserId) }
                }.decodeSingleOrNull<Profile>()

                if (currentUserProfile?.isAdmin != true) {
                    withContext(Dispatchers.Main) { callback(false, "Unauthorized: Admin only") }
                    return@launch
                }

                // Find the target user profile
                val targetProfile = client.from("profile").select {
                    filter { eq("display_name", displayName) }
                }.decodeSingleOrNull<Profile>()

                if (targetProfile == null || targetProfile.id == null) {
                    withContext(Dispatchers.Main) { callback(false, "User not found") }
                    return@launch
                }

                // Update the isAdmin flag and save back to database
                targetProfile.isAdmin = true

                // Use a temporary repo object to update the profile table
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
     * Removes a follow relationship between the current user and another user.
     * 
     * @param displayName The display name of the user to unfollow.
     * @param callback Callback returning true if the deletion was successful.
     */
    open fun removeFriendByDisplayName(displayName: String, callback: (Boolean) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id
            if (currentUserId == null) {
                withContext(Dispatchers.Main) { callback(false) }
                return@launch
            }

            try {
                // First find the user ID corresponding to the display name
                val profile = client.from("profile").select {
                    filter { eq("display_name", displayName) }
                }.decodeSingleOrNull<Profile>()

                if (profile == null || profile.id == null) {
                    withContext(Dispatchers.Main) { callback(false) }
                    return@launch
                }

                // Delete the record from 'follow' table where follower_id is current and user_id is target
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
