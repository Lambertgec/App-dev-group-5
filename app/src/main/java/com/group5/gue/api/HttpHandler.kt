package com.group5.gue.api

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import com.group5.gue.data.Result
import java.io.IOException

/**
 * Class that handles HTTP requests for external services not covered by the Supabase SDK.
 * Currently used to invoke Supabase Edge Functions for complex administrative tasks.
 */
class HttpHandler {

    // The OkHttpClient instance used to execute network requests.
    private val client = OkHttpClient()

    /**
     * Sends a POST request to the custom 'delete-account' Edge Function.
     * This function handles cleaning up user-related data across multiple tables.
     * 
     * @param accessToken The current user's JWT access token for authorization.
     * @param callback Function to be called with the success or failure result.
     */
    fun deleteAccount(accessToken: String, callback: (Result<Void>) -> Unit) {

        // Build the request targeting the Supabase Edge Function URL
        val request = Request.Builder()
            .url("https://wvcpjygatizwlusdgwno.supabase.co/functions/v1/delete-account")
            .post(ByteArray(0).toRequestBody(null))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .build()

        // Enqueue the request for asynchronous execution
        client.newCall(request).enqueue(object : Callback {
            // Called when the request could not be executed due to cancellation or connectivity issues.
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.Error(Exception("Delete account request failed", e)))
            }

            // Called when the HTTP response was successfully received from the server.
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        callback(Result.Success<Void>(null))
                    } else {
                        // Handle non-2xx status codes
                        callback(
                            Result.Error(
                                Exception("Delete account failed with HTTP ${it.code}")
                            )
                        )
                    }
                }
            }
        })
    }
}