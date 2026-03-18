package com.group5.gue.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AnnotationInsert(
    val building: String? = null,
    @SerialName("room_name")
    val roomName: String? = null,
    val level: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("creator_id")
    val creatorId: String? = null
)