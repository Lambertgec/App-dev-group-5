package com.group5.gue.data.annotation

import com.group5.gue.data.model.Annotation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AnnotationRepositoryTest {

    @Test
    fun testInstance() {
        val repo = AnnotationRepository.getInstance()
        assertNotNull(repo)
        val repo2 = AnnotationRepository.getInstance()
        assertEquals(repo, repo2)
    }

    @Test
    fun testTableName() {
        val repo = AnnotationRepository.getInstance()
        assertEquals("annotation", repo.tableName)
    }

    @Test
    fun testRepositoryMethodsPaths() {
        val repo = AnnotationRepository.getInstance()
        
        repo.getAll { _ -> }
        repo.getById(1L) { _ -> }
        repo.getByCreatorId("creator") { _ -> }
        repo.create(Annotation()) { _ -> }
        repo.update(1L, Annotation()) { _ -> }
        repo.delete(1L) { _ -> }
        
        assertNotNull(repo)
    }
}
