package com.group5.gue.data.annotation

import com.group5.gue.api.BaseRepository
import com.group5.gue.api.delete
import com.group5.gue.api.fetchAll
import com.group5.gue.api.fetchList
import com.group5.gue.api.fetchSingle
import com.group5.gue.api.insert
import com.group5.gue.api.update
import com.group5.gue.data.model.Annotation
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
            val created = insert(annotation)
            withContext(Dispatchers.Main) { callback(created) }
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
