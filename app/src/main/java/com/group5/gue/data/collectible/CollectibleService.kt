package com.group5.gue.data.collectible

import android.util.Log
import com.group5.gue.api.FileRepository
import com.group5.gue.data.Result
import com.group5.gue.data.model.Collectible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Handles interaction with Storage for collectibles
 * This should be called for uploading collectibles as it will then call the repository
 *  to insert the respective database entry
 */
class CollectibleService private constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val fileRepository = FileRepository.getInstance()
    private val collectibleRepository = CollectibleRepository.getInstance()

    private val bucketName = "collectible_img"

    companion object {
        private const val TAG = "CollectibleService"

        @Volatile
        private var instance: CollectibleService? = null

        fun getInstance(): CollectibleService {
            return instance ?: synchronized(this) {
                instance ?: CollectibleService().also { instance = it }
            }
        }
    }

    /**
     * Upload collectible image then call [CollectibleRepository] to insert DB entry
     *
     * @param collectible Collectible data
     * @param imageBytes Image file as byte array
     * @param fileExtension File extension 
     * @return Result with the created Collectible or an error if upload or insert fails
     */

    suspend fun createCollectible(collectible : Collectible, imageBytes: ByteArray, normalizedExtension: String): Result<Collectible> {
        val result = collectible.validated()
        if (result is Result.Error) {
            return result
        }

        val uploadedPath = fileRepository.uploadFile(
            bucketName = bucketName,
            byteArray = imageBytes,
            fileExtension = normalizedExtension,
            folderPath = "collectibles",
            upsert = false
        ) ?: return Result.Error(Exception("Failed to upload collectible image"))

        val publicImageUrl = fileRepository.getPublicImageUrl(bucketName, uploadedPath)
        val collectibleWithUrl = collectible.copy(imageUrl = publicImageUrl)
        val insertResult = collectibleRepository.insertCollectible(
            collectible = collectibleWithUrl
        )

        if (insertResult is Result.Error) {
            // Roll back the uploaded file if insert failes -> avoid unused images in storage
            val rollbackOk = fileRepository.deleteFile(bucketName, uploadedPath)
            if (!rollbackOk) {
                Log.w(TAG, "Rollback delete failed for uploadedPath=$uploadedPath")
            }
        }

        return insertResult
    }

    /**
     * Conveniency method for java callers 
     * 
     * @param collectible Collectible data
     * @param imageBytes Image file as byte array
     * @param fileExtension File extension 
     * @param callback Callback to receive the result with the created Collectible or an error if upload or insert fails
     */

    fun createCollectible(
        collectible : Collectible,
        imageBytes: ByteArray,
        fileExtension: String,
        callback: (Result<Collectible>) -> Unit
    ) {
        scope.launch {
            val result = createCollectible(
                collectible = collectible,
                imageBytes = imageBytes,
                normalizedExtension = fileExtension
            )
            withContext(Dispatchers.Main) { callback(result) }
        }
    }
}
