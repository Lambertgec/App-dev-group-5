package com.group5.gue.data.model

import com.group5.gue.data.Result
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    //Todo: add for other collectibles as well bc it looks nice
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
