package com.group5.gue.data.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String = "",
    val admin: Boolean = false
) {
    val role: Role
        get() = if (admin) Role.ADMIN else Role.USER
}
