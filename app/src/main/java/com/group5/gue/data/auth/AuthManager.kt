package com.group5.gue.data.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.group5.gue.api.SupabaseProvider
import com.group5.gue.data.Result
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID

private const val GOOGLE_CLIENT_ID =
    "169457876103-h5ap7v1s8tbbghhuqpe5gdajk8adomah.apps.googleusercontent.com"

class AuthManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val supabase = SupabaseProvider.supabaseClient
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "AuthManager"

        @Volatile
        private var INSTANCE: AuthManager? = null

        fun getInstance(context: Context): AuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthManager(context).also { INSTANCE = it }
            }
        }
    }

    fun signInWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result: Result<Void> = try {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.Success(null)
            } catch (e: Exception) {
                Log.e(TAG, "Email sign-in failed for $email", e)
                Result.Error(Exception(e.localizedMessage ?: "Sign in failed", e))
            }
            withContext(Dispatchers.Main) { callback.onResult(result) }
        }
    }

    fun signUpWithEmail(email: String, password: String, callback: AuthCallback) {
        scope.launch {
            val result: Result<Void> = try {
                supabase.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
                Result.Success(null)
            } catch (e: Exception) {
                Log.e(TAG, "Email sign-up failed for $email", e)
                Result.Error(Exception(e.localizedMessage ?: "Sign up failed", e))
            }
            withContext(Dispatchers.Main) { callback.onResult(result) }
        }
    }

    fun signInWithGoogle(callback: AuthCallback) {
        scope.launch(Dispatchers.Main.immediate) {
            val result: Result<Void> = try {
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
                Result.Success(null)
            } catch (e: Exception) {
                Log.e(TAG, "Google sign-in failed", e)
                Result.Error(Exception(e.localizedMessage ?: "Google sign in failed", e))
            }
            callback.onResult(result)
        }
    }

    fun logout(callback: AuthCallback) {
        scope.launch(Dispatchers.Main.immediate) {
            val result: Result<Void> = try {
                supabase.auth.signOut()
                Result.Success(null)
            } catch (e: Exception) {
                Log.e(TAG, "Logout failed", e)
                Result.Error(Exception(e.localizedMessage ?: "Logout failed", e))
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
    fun onResult(result: Result<Void>)
}
