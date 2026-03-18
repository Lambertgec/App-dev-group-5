package com.group5.gue.data.user

import com.group5.gue.api.BaseRepository
import com.group5.gue.api.fetchSingle
import com.group5.gue.api.update
import com.group5.gue.api.insert
import com.group5.gue.data.Result
import com.group5.gue.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository private constructor() : BaseRepository {

    override val tableName = "profile"

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

    fun getCachedUser(): User? = cachedUser

    fun setCachedUser(user: User?) { cachedUser = user }    
}