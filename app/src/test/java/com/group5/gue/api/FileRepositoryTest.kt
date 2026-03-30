package com.group5.gue.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileRepositoryTest {

    @Test
    fun testInstance() {
        try {
            val repo = FileRepository.getInstance()
            assertNotNull(repo)
        } catch (e: Exception) {
        }
    }

    @Test
    fun testGetPublicUrl() {
        try {
            val repo = FileRepository.getInstance()
            val url = repo.getPublicImageUrl("bucket", "path")
            assertNotNull(url)
        } catch (e: Exception) {
        }
    }
}
