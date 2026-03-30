package com.group5.gue.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.group5.gue.data.user.UserRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.group5.gue.api.SupabaseProvider
import com.group5.gue.data.Result
import com.group5.gue.data.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.security.MessageDigest
import java.util.UUID

private const val GOOGLE_CLIENT_ID =
    "169457876103-h5ap7v1s8tbbghhuqpe5gdajk8adomah.apps.googleusercontent.com"

/**
 * Exposes kotlin based Supabase AUTH to Java UI
 * 
 * Handles sign in, sign up and logout process
 */

class AuthManager private constructor(context: Context) {

    // App context
    private val appContext = context.applicationContext
    // Supabase client
    private val supabase = SupabaseProvider.supabaseClient
    // User repository
    private val userRepository = UserRepository.getInstance()
    // Coroutine scope
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "AuthManager"

        @Volatile
        private var INSTANCE: AuthManager? = null

        /**
         * Singleton instance of AuthManager
         */
        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context).also { INSTANCE = it }
            }
        }
    }

    /**
     * Logs in user by email, and then fetches the user profile from the database
     * 
     * @param email
     * @param password
     * 
     * @return Result with the logged in User or an error if login or fetching fails
     */

    private suspend fun signInWithEmail(email: String, password: String): Result<User> {
        return try {
            // Sign in with email
            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
            // Save the current session
            val session = supabase.auth.currentSessionOrNull()
            // Save the current user
            val currentUser = supabase.auth.currentUserOrNull()
            // In case there is a valid session with valid user
            // Update the cached user in the repository
            if (session != null && currentUser != null) {
                val userId = currentUser.id
                val userResult = userRepository.fetchUserById(userId, cache = true)
                if (userResult is Result.Success) {
                    Result.Success(userResult.data)
                } else {
                    Result.Error(Exception("Failed to fetch user after sign-in"))
                }
            } else {
                Result.Error(Exception("Sign in failed: No session returned"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-in failed for $email", e)
            Result.Error(Exception(e.localizedMessage ?: "Sign in failed for $email", e))
        }
    }

    /**
     * Signs up user by email, then creates a new user profile in the database
     * 
     * @param email
     * @param password
     * 
     * @return Result with the signed up User or an error if sign up or user creation fails
     */
    private suspend fun signUpWithEmail(email: String, password: String): Result<User> {
        return try {
            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
            val currentUser = supabase.auth.currentUserOrNull()
            if (currentUser != null) {
                val newUser = User(
                    id = currentUser.id,
                    name = email.substringBefore('@'),
                    score = 0,
                    isAdmin = false
                )
                val createResult = userRepository.createUser(newUser)
                if (createResult is Result.Success) {
                    Result.Success(createResult.data)
                } else {
                    Result.Error(Exception("Failed to create user after sign-up"))
                }
            } else {
                Result.Error(Exception("Sign up failed: No user returned"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Email sign-up failed for $email", e)
            Result.Error(Exception(e.localizedMessage ?: "Sign up failed for $email", e))
        }
    }

    /**
     * Signs in user with Google, then fetches or creates the user profile in the database
     * 
     * @return Result with the signed in User or an error if sign in or fetching/creation fails
     */
    private suspend fun signInWithGoogle(): Result<User> {
        return try {
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

            val credentialManager = CredentialManager.create(appContext)
            val credential = credentialManager.getCredential(
                context = appContext,
                request = request
            )

            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(credential.credential.data)

            supabase.auth.signInWith(IDToken) {
                idToken = googleIdTokenCredential.idToken
                provider = Google
                nonce = noncePair.raw
            }

            val session = supabase.auth.currentSessionOrNull()
            val currentUser = supabase.auth.currentUserOrNull()
            if (session != null && currentUser != null) {
                val userId = currentUser.id
                val userResult = userRepository.fetchUserById(userId, cache = true)
                if (userResult is Result.Success) {
                    Result.Success(userResult.data)
                } else {
                    val newUser = User(
                        id = userId,
                        name = currentUser.email?.substringBefore('@') ?: "Google User",
                        score = 0,
                        isAdmin = false
                    )
                    val createResult = userRepository.createUser(newUser)
                    if (createResult is Result.Success) {
                        Result.Success(createResult.data)
                    } else {
                        Result.Error(Exception("Failed to create user after Google sign-in"))
                    }
                }
            } else {
                Result.Error(Exception("Google sign-in failed: No session returned"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Google sign-in failed", e)
            Result.Error(Exception(e.localizedMessage ?: "Google sign in failed", e))
        }
    }

    /**
     * Logs out the current user
     * 
     * @return Result with null on success or an error if logout fails
     */
    private suspend fun logout(): Result<Void> {
        return try {
            supabase.auth.signOut()
            userRepository.setCachedUser(null)
            Result.Success(null)
        } catch (e: Exception) {
            Log.e(TAG, "Logout failed", e)
            Result.Error(Exception(e.localizedMessage ?: "Logout failed", e))
        }
    }

    /**
     * Check if there is an existing session stored and load that user
     * 
     * @return Result with the signed in User or an error if fetching fails
     */
    private suspend fun getUserFromSession(): Result<User> {
        val status = withTimeoutOrNull(5000L) {
            supabase.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        } ?: return Result.Error(Exception("Session status check timed out"))

        return try {
            val userId = when (status) {
                is SessionStatus.Authenticated -> supabase.auth.currentUserOrNull()?.id
                is SessionStatus.NotAuthenticated -> null
                is SessionStatus.RefreshFailure -> null
                SessionStatus.Initializing -> null
            } ?: return Result.Error(Exception("No authenticated user"))

            val userResult = userRepository.fetchUserById(userId, cache = true)
            if (userResult is Result.Success) {
                Result.Success(userResult.data)
            } else {
                Result.Error(Exception("Failed to fetch user from session"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user from session", e)
            Result.Error(Exception(e.localizedMessage ?: "Failed to get user from session", e))
        }
    }

    // Convenience methods for Java callers to execute suspend functions and receive results via callback

    /**
     * Handle signing in via email
     * @param email Email
     * @param password Password
     * @param callback Callback with the result
     */
    fun signInWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result = signInWithEmail(email, password)
            withContext(Dispatchers.Main) { callback.onResult(result) }
        }
    }

    /**
     * Handle signing up via email
     * @param email Email
     * @param password Password
     * @param callback Callback with the result
     */
    fun signUpWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result = signUpWithEmail(email, password)
            withContext(Dispatchers.Main) { callback.onResult(result) }
        }
    }

    /**
     * Handle signing up via Google account
     * @param callback Callback with the result
     */
    fun signInWithGoogle(callback: AuthCallback) {
        scope.launch {
            val result = signInWithGoogle()
            withContext(Dispatchers.Main) { callback.onResult(result) }
        }
    }

    /**
     * Handle logging out
     * @param callback Callback with the result
     */
    fun logout(callback: (Result<Void>) -> Unit) {
        scope.launch {
            val result = logout()
            withContext(Dispatchers.Main) { callback(result) }
        }
    }

    /**
     * Get the current user from the session
     * @param callback Callback with the result
     */
    fun getUserFromSession(callback: AuthCallback) {
        scope.launch {
            val cachedUser = getUserFromSession()
            withContext(Dispatchers.Main) { callback.onResult(cachedUser) }
        }
    }

    /**
     * Creates a nonce pair for authentication
     * 
     * @return NoncePair with raw and hashed nonces
     */

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

