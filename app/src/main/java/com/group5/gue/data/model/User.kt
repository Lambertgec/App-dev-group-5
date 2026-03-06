package com.group5.gue.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class User (
    val id: String = "",
    @SerialName("display_name")
    val name: String = "",
    val score: Int = 0,
    @SerialName("is_admin")
    val isAdmin: Boolean = false,
) {
    val role: Role
        get() = if (isAdmin) Role.ADMIN else Role.USER
}









