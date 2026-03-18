package com.group5.gue.api

import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import com.group5.gue.data.Result
import java.io.IOException

// Class that handles http request - Used to access edge functions
class HttpHandler {

    private val client = OkHttpClient()
    /**
     * Sends a POST request to the delete account endpoint with the provided access token
     */
    fun deleteAccount(accessToken: String, callback: (Result<Void>) -> Unit) {

        val request = Request.Builder()
            .url("https://wvcpjygatizwlusdgwno.supabase.co/functions/v1/delete-account")
            .post(ByteArray(0).toRequestBody(null))
            .header("Authorization", "Bearer $accessToken")
            .header("Content-Type", "application/json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(Result.Error(Exception("Delete account request failed", e)))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (it.isSuccessful) {
                        callback(Result.Success<Void>(null))
                    } else {
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