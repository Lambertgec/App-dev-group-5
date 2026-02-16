package com.group5.gue.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.group5.gue.data.Result
import com.group5.gue.data.model.User
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import com.group5.gue.data.supabase.SupabaseClientProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import io.github.jan.supabase.auth.status.SessionStatus
import java.security.MessageDigest
import java.util.UUID

private const val GOOGLE_CLIENT_ID = "169457876103-h5ap7v1s8tbbghhuqpe5gdajk8adomah.apps.googleusercontent.com"

class AuthDataSource(
    private val context: Context
) {
    private val supabase = SupabaseClientProvider.client

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun signUpWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result: Result<User> = try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.Success(User(email, false))
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
                Result.Success(User(email, false))
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
                Result.Success(User("Google User", false))
            } catch (e: Exception) {
                Result.Error<User>(Exception(e.localizedMessage ?: "Google sign in failed", e))
            }

            callback.onResult(result)
        }
    }

    fun logout(callback: AuthCallback) {
        scope.launch(Dispatchers.Main.immediate) {
            val result: Result<User> = try {
                supabase.auth.signOut()
                Result.Success(User("", false))
            } catch (e: Exception) {
                Result.Error<User>(Exception(e.localizedMessage ?: "Logout failed", e))
            }

            callback.onResult(result)
        }
    }

    fun getCachedUserId(callback: UserIdCallback) {
        scope.launch {
            val userId = awaitUserIdFromSession()
            withContext(Dispatchers.Main) {
                callback.onResult(userId)
            }
        }
    }

    private suspend fun awaitUserIdFromSession(): String? {
        val status = supabase.auth.sessionStatus.first { it !is SessionStatus.Initializing }
        return when (status) {
            is SessionStatus.Authenticated -> status.session.user?.id
            is SessionStatus.NotAuthenticated -> null
            is SessionStatus.RefreshFailure -> null
            SessionStatus.Initializing -> null
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


fun interface UserIdCallback {
    fun onResult(userId: String?)
}
