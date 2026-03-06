package com.group5.gue.data.user

import com.group5.gue.api.BaseRepository
import com.group5.gue.api.fetchSingle
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

    fun getCachedUser(): User? = cachedUser

    fun setCachedUser(user: User?) { cachedUser = user }

    fun fetchAndCacheUser(userId: String, callback: (User?) -> Unit) {
        scope.launch {
            val user = fetchUserWithRetry(userId)
            cachedUser = user
            withContext(Dispatchers.Main) { callback(user) }
        }
    }

    private suspend fun fetchUserWithRetry(userId: String): User? {
        val maxAttempts = 5
        val retryDelayMs = 250L

        repeat(maxAttempts) { attempt ->
            val user = fetchSingle<User>("id", userId)
            if (user != null && user.id.isNotBlank()) {
                return user
            }
            if (attempt < maxAttempts - 1) {
                delay(retryDelayMs)
            }
        }
        return null
    }
}