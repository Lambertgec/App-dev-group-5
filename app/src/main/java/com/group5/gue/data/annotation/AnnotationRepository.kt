package com.group5.gue.data.annotation

import android.util.Log
import com.group5.gue.api.BaseRepository
import com.group5.gue.api.delete
import com.group5.gue.api.fetchAll
import com.group5.gue.api.fetchList
import com.group5.gue.api.fetchSingle
import com.group5.gue.api.insert
import com.group5.gue.api.update
import com.group5.gue.data.model.Annotation
import com.group5.gue.data.model.AnnotationInsert
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Repository class for managing Annotation data.
 * It provides methods to perform operations on the 'annotation' table in Supabase.
 */
class AnnotationRepository private constructor() : BaseRepository {

    // The name of the database table for annotations.
    override val tableName = "annotation"

    // Coroutine scope for executing database operations in the background.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        @Volatile
        private var instance: AnnotationRepository? = null

        /**
         * Returns the singleton instance of AnnotationRepository.
         *
         * @return The AnnotationRepository instance.
         */
        fun getInstance(): AnnotationRepository {
            return instance ?: synchronized(this) {
                instance ?: AnnotationRepository().also { instance = it }
            }
        }
    }

    /**
     * Retrieves all annotations from the database.
     *
     * @param callback A function to be called with the list of retrieved annotations.
     */
    fun getAll(callback: (List<Annotation>) -> Unit) {
        scope.launch {
            val rows = fetchAll<Annotation>()
            withContext(Dispatchers.Main) { callback(rows) }
        }
    }

    /**
     * Retrieves a single annotation by its ID.
     *
     * @param id The ID of the annotation to retrieve.
     * @param callback A function to be called with the retrieved annotation or null if not found.
     */
    fun getById(id: Long, callback: (Annotation?) -> Unit) {
        scope.launch {
            val row = fetchSingle<Annotation>("id", id)
            withContext(Dispatchers.Main) { callback(row) }
        }
    }

    /**
     * Retrieves all annotations created by a specific user.
     *
     * @param creatorId The ID of the user who created the annotations.
     * @param callback A function to be called with the list of annotations.
     */
    fun getByCreatorId(creatorId: String, callback: (List<Annotation>) -> Unit) {
        scope.launch {
            val rows = fetchList<Annotation>("creator_id", creatorId)
            withContext(Dispatchers.Main) { callback(rows) }
        }
    }

    /**
     * Creates a new annotation in the database.
     *
     * @param annotation The annotation object containing the data to insert.
     * @param callback A function to be called with the created annotation or null on failure.
     */
    fun create(annotation: Annotation, callback: (Annotation?) -> Unit) {
        scope.launch {
            val insertPayload = AnnotationInsert(
                building = annotation.building,
                roomName = annotation.roomName,
                level = annotation.level,
                latitude = annotation.latitude,
                longitude = annotation.longitude,
                creatorId = annotation.creatorId
            )
            try {
                val created = client.postgrest
                    .from(tableName)
                    .insert(insertPayload) { select() }
                    .decodeSingle<Annotation>()
                withContext(Dispatchers.Main) { callback(created) }
            } catch (e: Exception) {
                Log.e("ANNOTATION_CREATE", "Full error: ${e::class.simpleName}: ${e.message}", e)
                withContext(Dispatchers.Main) { callback(null) }
            }
        }
    }

    /**
     * Updates an existing annotation in the database.
     *
     * @param id The ID of the annotation to update.
     * @param annotation The annotation object with updated fields.
     * @param callback A function to be called with the updated annotation or null on failure.
     */
    fun update(id: Long, annotation: Annotation, callback: (Annotation?) -> Unit) {
        scope.launch {
            val updated = update("id", id, annotation)
            withContext(Dispatchers.Main) { callback(updated) }
        }
    }

    /**
     * Deletes an annotation from the database.
     *
     * @param id The ID of the annotation to delete.
     * @param callback A function to be called with true if deletion was successful, false otherwise.
     */
    fun delete(id: Long, callback: (Boolean) -> Unit) {
        scope.launch {
            val ok = delete<Annotation>("id", id)
            withContext(Dispatchers.Main) { callback(ok) }
        }
    }
}
