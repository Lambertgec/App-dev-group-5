package com.group5.gue.data.repository

import com.group5.gue.data.Result
import com.group5.gue.data.model.User
import com.group5.gue.data.supabase.SupabaseClientProvider
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserRepository(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val supabase = SupabaseClientProvider.client

    fun getUserById(userId: String, callback: UserResultCallback) {
        scope.launch {
            val result: Result<User> = try {
                val user = supabase
                    .from("profile")
                    .select {
                        filter { eq("id", userId) }
                        limit(1)
                    }
                    .decodeSingle<User>()

                Result.Success(user)
            } catch (e: Exception) {
                Result.Error(Exception(e.localizedMessage ?: "Failed to load user", e))
            }

            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }

}

fun interface UserResultCallback {
    fun onResult(result: Result<User>)
}
