package com.group5.gue.data.friends

import android.util.Log
import com.group5.gue.api.BaseRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import com.group5.gue.api.delete
import com.group5.gue.api.fetchList
import com.group5.gue.api.insert
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

@Serializable
data class Profile(
    @SerialName("id") val id: String? = null,
    @SerialName("display_name") val displayName: String
)

@Serializable
data class FollowEntry(
    @SerialName("user_id") val userID: String,
    val profile: Profile? = null
)

class FriendsRepository private constructor() : BaseRepository {

    override val tableName = "follow"
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        @Volatile
        private var instance: FriendsRepository? = null

        @JvmStatic
        fun getInstance(): FriendsRepository {
            return instance ?: synchronized(this) {
                instance ?: FriendsRepository().also { instance = it }
            }
        }
    }

    /**
     * Fetch the list of friends for the current user signed in.
     */
    fun fetchFriends(callback: (List<String>) -> Unit) {
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

    /*
     * Add a friend by their display name.
     */
    fun addFriendByDisplayName(displayName: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            val currentUserId = client.auth.currentSessionOrNull()?.user?.id
            if (currentUserId == null) {
                withContext(Dispatchers.Main) {
                    callback(false, "You must be logged in.")
                }
                return@launch
            }

            try {
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

                val result = insert(newFollow)
                withContext(Dispatchers.Main) {
                    if (result != null) callback(true, "Followed ${profile.displayName}!")
                    else callback(false, "Failed to update database")
                }
            } catch (e: Exception) {
                Log.e("FriendsRepository", "Add failed", e)
                withContext(Dispatchers.Main) { callback(false, "Error: ${e.message}") }
            }
        }
    }

    /**
     * Remove a friend by their display name.
     */
    fun removeFriendByDisplayName(displayName: String, callback: (Boolean) -> Unit) {
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
