package com.group5.gue.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Follow(
    @SerialName("user_id")
    val userID: String,
    @SerialName("follower_id")
    val followerID: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
