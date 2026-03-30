package com.group5.gue.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a follow relationship between two users.
 * This class tracks when one user follows another and stores the timestamp of the action.
 *
 * @property userID The unique identifier of the user being followed.
 * @property followerID The unique identifier of the user who is following.
 * @property createdAt The timestamp when the follow relationship was created.
 */
@Serializable
data class Follow(
    @SerialName("user_id")
    val userID: String,
    @SerialName("follower_id")
    val followerID: String,
    @SerialName("created_at")
    val createdAt: String? = null
)
