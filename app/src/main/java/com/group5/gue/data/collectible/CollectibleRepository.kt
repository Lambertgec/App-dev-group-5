package com.group5.gue.data.collectible

import android.util.Log
import com.group5.gue.api.BaseRepository
import com.group5.gue.api.fetchAll
import com.group5.gue.api.insert
import com.group5.gue.api.delete
import com.group5.gue.data.Result
import com.group5.gue.data.model.Collectible
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


/**
 * Repository for handling Collectible table in Supabase
 */
class CollectibleRepository private constructor() : BaseRepository {

    override val tableName: String = "collectible"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "CollectibleRepository"

        @Volatile
        private var instance: CollectibleRepository? = null

        fun getInstance(): CollectibleRepository {
            return instance ?: synchronized(this) {
                instance ?: CollectibleRepository().also { instance = it }
            }
        }
    }

    /**
     * Insert a new collectible with given public url to the DB
     * 
     * @param collectible Collectible data
     * @return Result with the created Collectible or an error if insert fails
     */

    suspend fun insertCollectible(collectible: Collectible): Result<Collectible> {
        val result = collectible.validated()
        if (result is Result.Error) {
            return result
        }

        return try {
            insert<Collectible, Collectible>(collectible).let { Result.Success(it) }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert collectible", e)
            Result.Error(Exception("Failed to save collectible", e))
        }
    }

    /**
     * If no imageURL is provided, forces the upload to happen first
     */

    suspend fun insertCollectible(collectible: Collectible, imageBytes: ByteArray, fileExtension: String): Result<Collectible> {
        return CollectibleService.getInstance().createCollectible(
            collectible = collectible,
            imageBytes = imageBytes,
            normalizedExtension = fileExtension
        )
    }

    /**
     * Deletes collectible by id
     * Edge function handles deleting the image from storage
     * 
     * @param collectibleId ID of the collectible to delete
     * @return Result indicating success
     */

    suspend fun deleteCollectible(collectibleId: Int): Result<Unit> {
        return try {
            delete<Collectible>("id", collectibleId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete collectible", e)
            Result.Error(Exception("Failed to delete collectible", e))
        }
    }

    /**
     * Returns all collectibles from the database. Returns an empty list if fetch fails.
     */

    suspend fun getAllCollectibles(): List<Collectible> {
        return fetchAll()
    }

    // Conveniency method for java callers


    fun deleteCollectible(collectibleId: Int, callback: (Result<Unit>) -> Unit) {
        scope.launch {
            val result = deleteCollectible(collectibleId)
            withContext(Dispatchers.Main) { callback(result) }
        }
    }

    fun getAllCollectibles(callback: (List<Collectible>) -> Unit) {
        scope.launch {
            val collectibles = try {
                getAllCollectibles()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch collectibles", e)
                emptyList()
            }
            withContext(Dispatchers.Main) { callback(collectibles) }
        }
    }
}
