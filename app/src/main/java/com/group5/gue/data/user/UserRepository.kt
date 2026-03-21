package com.group5.gue.data.user

import com.group5.gue.api.BaseRepository
import com.group5.gue.api.fetchTableList
import com.group5.gue.api.fetchSingle
import com.group5.gue.api.update
import com.group5.gue.api.insert
import com.group5.gue.data.Result
import com.group5.gue.data.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import com.group5.gue.api.HttpHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class UserRepository private constructor() : BaseRepository {

    override val tableName = "profile"
    private val ownershipTableName = "has_collectible"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var cachedUser: User? = null
    companion object {
        @Volatile
        private var instance: UserRepository? = null

        fun getInstance(): UserRepository {
            return instance ?: synchronized(this) {
                instance ?: UserRepository().also { instance = it }
            }
        }
    }

    /**
     * Creates a new User in the database
     * 
     * @param user User data
     * 
     * @return Result with the created User or an error if creation fails
     */
    suspend fun createUser(user: User): Result<User> {
        return try {
            val createdUser = insert<User, User>(user)
            if (createdUser != null) {
                Result.Success(createdUser)
            } else {
                Result.Error(Exception("Failed to create user"))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Failed to create user", e))
        }
    }

    /**
     * Fetches a User by their ID
     *
     * @param userId The ID of the User to fetch
     * @param cache Whether to cache the fetched User
     *
     * @return Result with the fetched User or an error if fetching fails
     */

    suspend fun fetchUserById(userId: String, cache: Boolean): Result<User> {
        return try {
            val user = fetchSingle<User>("id", userId)
            if (user != null) {
                if (cache) {
                    cachedUser = user
                }
                Result.Success(user)
            } else {
                Result.Error(Exception("User not found"))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Failed to fetch user", e))
        }
    }


    /**
     * Updates an existing User in the database
     * 
     * @param user new user data
     * @param userId the ID of the user to update, if empty it will use the ID from the user object
     * 
     * @return Result with the updated User or an error if update fails
     */
    private suspend fun updateUser(user: User, userId: String = ""): Result<User> {
        return try {
            val userToUpdate = userId.ifEmpty { user.id }
            val updatedUser = update<User>("id", userToUpdate, user)
            if (updatedUser != null) {
                cachedUser = updatedUser
                Result.Success(updatedUser)
            } else {
                Result.Error(Exception("Failed to update user"))
            }
        } catch (e: Exception) {
            Result.Error(Exception("Failed to update user", e))
        }
    }

    /**
     * Deletes the current user's account
     *
     * @param callback The callback to handle the result
     */
    fun deleteAccount(callback: (Result<Void>) -> Unit) {
        client.auth.currentSessionOrNull()?.accessToken?.let { accessToken ->
            HttpHandler().deleteAccount(accessToken, callback)
        } ?: callback(Result.Error(Exception("No access token available")))
    }

    // Convenience function for Java
    fun updateUser(user: User, callback: UserUpdateCallback) {
        scope.launch {
            val result = updateUser(user)
            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }

    fun getCachedUser(): User? = cachedUser

    fun setCachedUser(user: User?) { cachedUser = user }

    @Serializable
    private data class CollectibleId(
        @SerialName("collectible_id") val collectibleId: Int
    )

    suspend fun getOwnedCollectibleIds(userId: String): Set<Int> {
        if (userId.isBlank()) {
            return emptySet()
        }

        return fetchTableList<CollectibleId>(
            targetTable = ownershipTableName,
            columns = Columns.raw("collectible_id"),
            column = "user_id",
            value = userId
        )
            .map { it.collectibleId }
            .toSet()
    }

    fun getOwnedCollectibleIds(userId: String, callback: (Set<Int>) -> Unit) {
        scope.launch {
            val ownedIds = getOwnedCollectibleIds(userId)
            withContext(Dispatchers.Main) { callback(ownedIds) }
        }
    }

}

fun interface UserUpdateCallback {
    fun onResult(result: Result<User>)
}