package com.group5.gue.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a location-based annotation created by a user.
 * This class is used to store and retrieve annotation data from the database.
 *
 * @property id The unique identifier for the annotation.
 * @property createdAt The timestamp when the annotation was created.
 * @property building The name of the building where the annotation is located.
 * @property roomName The name of the room within the building.
 * @property level The floor level within the building.
 * @property latitude The geographical latitude of the annotation.
 * @property longitude The geographical longitude of the annotation.
 * @property creatorId The ID of the user who created this annotation.
 */
@Serializable
data class Annotation(
    val id: Long = 0L,
    @SerialName("created")
    val createdAt: String? = null,
    val building: String? = null,
    @SerialName("room_name")
    val roomName: String? = null,
    val level: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("creator_id")
    val creatorId: String? = null
)