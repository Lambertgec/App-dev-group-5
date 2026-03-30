package com.group5.gue.data.model

import com.group5.gue.data.Result
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Data class representing a collectible item in the application.
 * Collectibles are items that users can find or earn, which contribute to their score.
 *
 * @property id Unique identifier for the collectible.
 * @property creatorId The ID of the user who created this collectible.
 * @property name The display name of the collectible.
 * @property score The points awarded to a user for obtaining this collectible.
 * @property description A brief explanation of what the collectible is.
 * @property createdAt Timestamp of when the collectible was added.
 * @property imageUrl URL pointing to the image asset for this collectible.
 */
@Serializable
data class Collectible(
    val id: Int = 0,
    @SerialName("creator_id")
    val creatorId: String? = null   ,
    val name: String = "",
    val score: Int = 0,
    val description: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("image_url")
    val imageUrl: String? = null
) {
    /**
     * Validates the collectible's data before processing or saving.
     * Checks if the name is not empty and the score is non-negative.
     *
     * @return A [Result] indicating success with the collectible or an error message.
     */
    fun validated(): Result<Collectible> {
        val normalizedName = name.trim()
        if (normalizedName.isBlank()) {
            return Result.Error(Exception("Collectible name is required"))
        }

        if (score < 0) {
            return Result.Error(Exception("Score cannot be negative"))
        }

        return Result.Success(this)
    }
}
