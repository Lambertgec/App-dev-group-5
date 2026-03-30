package com.group5.gue.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Represents a user in the system.
 * This class holds basic profile information and system-level permissions.
 *
 * @property id The unique identifier for the user, from Supabase Auth.
 * @property name The display name of the user.
 * @property score The current points earned by the user.
 * @property isAdmin Boolean flag indicating if the user has administrative privileges.
 */
@Serializable
data class User (
    val id: String = "",
    @SerialName("display_name")
    val name: String? = null,
    val score: Int = 0,
    @SerialName("is_admin")
    val isAdmin: Boolean = false,
) {
    /**
     * Determines the user's role based on their admin status.
     * @return [Role.ADMIN] if the user is an administrator, [Role.USER] otherwise.
     */
    val role: Role
        get() = if (isAdmin) Role.ADMIN else Role.USER
}
