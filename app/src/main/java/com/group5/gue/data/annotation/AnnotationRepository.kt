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

class AnnotationRepository private constructor() : BaseRepository {

    override val tableName = "annotation"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        @Volatile
        private var instance: AnnotationRepository? = null

        fun getInstance(): AnnotationRepository {
            return instance ?: synchronized(this) {
                instance ?: AnnotationRepository().also { instance = it }
            }
        }
    }

    fun getAll(callback: (List<Annotation>) -> Unit) {
        scope.launch {
            val rows = fetchAll<Annotation>()
            withContext(Dispatchers.Main) { callback(rows) }
        }
    }

    fun getById(id: Long, callback: (Annotation?) -> Unit) {
        scope.launch {
            val row = fetchSingle<Annotation>("id", id)
            withContext(Dispatchers.Main) { callback(row) }
        }
    }

    fun getByCreatorId(creatorId: String, callback: (List<Annotation>) -> Unit) {
        scope.launch {
            val rows = fetchList<Annotation>("creator_id", creatorId)
            withContext(Dispatchers.Main) { callback(rows) }
        }
    }

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

    fun update(id: Long, annotation: Annotation, callback: (Annotation?) -> Unit) {
        scope.launch {
            val updated = update("id", id, annotation)
            withContext(Dispatchers.Main) { callback(updated) }
        }
    }

    fun delete(id: Long, callback: (Boolean) -> Unit) {
        scope.launch {
            val ok = delete<Annotation>("id", id)
            withContext(Dispatchers.Main) { callback(ok) }
        }
    }
}
