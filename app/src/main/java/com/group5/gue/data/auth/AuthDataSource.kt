package com.group5.gue.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.group5.gue.data.Result
import com.group5.gue.data.model.Role
import com.group5.gue.data.model.User
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

private const val SUPABASE_URL = "https://wvcpjygatizwlusdgwno.supabase.co"
private const val SUPABASE_KEY = "sb_publishable_p5UsVza2oMOi-P2jGDX3xg_7GtMduWT"
private const val GOOGLE_CLIENT_ID = "169457876103-h5ap7v1s8tbbghhuqpe5gdajk8adomah.apps.googleusercontent.com"

// De-serialize Supabase JSON display_name
@Serializable
data class Profile(
    @SerialName("display_name") val displayName: String
)

// De-serialize Supabase JSON follower_id
@Serializable
data class FollowEntry(
    @SerialName("follower_id") val followerID: String,
    val profile: Profile? = null
)

class AuthDataSource(
    private val context: Context
) {
    private val prefs = context.getSharedPreferences("auth_tokens", Context.MODE_PRIVATE)

    private val supabase = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Auth)
        install(Postgrest)
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun signUpWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result: Result<User> = try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Save session tokens for persistence
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    prefs.edit().apply {
                        putString("access_token", session.accessToken)
                        putString("refresh_token", session.refreshToken)
                        apply()
                    }
                }
                Result.Success(User(email, Role.USER))
            } catch (e: Exception) {
                Result.Error<User>(Exception(e.localizedMessage ?: "Sign up failed", e))
            }

            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }

    fun signInWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result: Result<User> = try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                // Save session tokens for persistence
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    prefs.edit().apply {
                        putString("access_token", session.accessToken)
                        putString("refresh_token", session.refreshToken)
                        apply()
                    }
                }
                Result.Success(User(email, Role.USER))
            } catch (e: Exception) {
                Result.Error<User>(Exception(e.localizedMessage ?: "Sign in failed", e))
            }

            withContext(Dispatchers.Main) {
                callback.onResult(result)
            }
        }
    }

    fun signInWithGoogle(callback: AuthCallback) {
        scope.launch(Dispatchers.Main.immediate) {
            val result: Result<User> = try {
                val noncePair = createNonce()

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId(GOOGLE_CLIENT_ID)
                    .setNonce(noncePair.hashed)
                    .setAutoSelectEnabled(false)
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val credentialManager = CredentialManager.create(context)
                val credential = credentialManager.getCredential(
                    context = context,
                    request = request
                )

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(credential.credential.data)

                supabase.auth.signInWith(IDToken) {
                    idToken = googleIdTokenCredential.idToken
                    provider = Google
                    nonce = noncePair.raw
                }
                // Save session tokens for persistence
                val session = supabase.auth.currentSessionOrNull()
                if (session != null) {
                    prefs.edit().apply {
                        putString("access_token", session.accessToken)
                        putString("refresh_token", session.refreshToken)
                        apply()
                    }
                }
                Result.Success(User("Google User", Role.USER))
            } catch (e: Exception) {
                Result.Error<User>(Exception(e.localizedMessage ?: "Google sign in failed", e))
            }

            callback.onResult(result)
        }
    }

    fun getCachedUserId(): String? {
        val session = supabase.auth.currentSessionOrNull()
        if (session != null) {
            return session.user?.id
        }
        return null
     }

    fun getCachedUserIdAsync(callback: UserIdCallback) {
        scope.launch {
            // Restore tokens from SharedPreferences on cold start
            val accessToken = prefs.getString("access_token", null)
            val refreshToken = prefs.getString("refresh_token", null)
            
            if (accessToken != null && refreshToken != null) {
                try {
                    supabase.auth.refreshSession(refreshToken)
                } catch (e: Exception) {
                    // Refresh failed, tokens may be expired
                }
            }
            
            var userId = getCachedUserId()
            if (userId == null) {
                delay(250)
                userId = getCachedUserId()
            }
            withContext(Dispatchers.Main) {
                callback.onResult(userId)
            }
        }
    }

    // Fetch friends from Supabase
    // Returns list of friends of cached (signed in) user
    // Joins follow and profile tables on follower_id and filters for current user
    fun getFollowedUsers(callback: (List<String>) -> Unit) {
        val currentUserId = getCachedUserId()
        if (currentUserId == null) {
            callback(emptyList())
            return
        }
        scope.launch {
            val friends = try {
                val response = supabase.postgrest["follow"].select(
                    Columns.raw("follower_id, profile!follower_id(display_name)")
                ) {
                    filter {
                        eq("user_id", currentUserId)
                    }
                }

                val entries = response.decodeList<FollowEntry>()
                entries.mapNotNull { it.profile?.displayName }

            } catch (e: Exception) {
                emptyList()
            }
            withContext(Dispatchers.Main) {
                callback(friends)
            }
        }
    }
    

    fun logout(callback: AuthCallback) {
        scope.launch(Dispatchers.Main.immediate) {
            val result: Result<User> = try {
                supabase.auth.signOut()
                // Clear stored tokens
                prefs.edit().apply {
                    remove("access_token")
                    remove("refresh_token")
                    apply()
                }
                Result.Success(User("", Role.USER))
            } catch (e: Exception) {
                Result.Error<User>(Exception(e.localizedMessage ?: "Logout failed", e))
            }

            callback.onResult(result)
        }
    }


    private fun createNonce(): NoncePair {
        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.joinToString("") { "%02x".format(it) }
        return NoncePair(raw = rawNonce, hashed = hashedNonce)
    }
}

data class NoncePair(val raw: String, val hashed: String)

fun interface AuthCallback {
   fun onResult(result: Result<User>)
}

fun interface RoleCallback {
    fun onResult(result: Result<Role>)
}

fun interface UserIdCallback {
    fun onResult(userId: String?)
}
