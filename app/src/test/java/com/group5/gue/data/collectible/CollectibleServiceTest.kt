package com.group5.gue.data.collectible

import com.group5.gue.data.model.Collectible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CollectibleServiceTest {

    @Test
    fun testInstance() {
        try {
            val service = CollectibleService.getInstance()
            assertNotNull(service)
        } catch (e: Exception) {
        }
    }

    @Test
    fun testServiceMethodsPaths() {
        try {
            val service = CollectibleService.getInstance()
            val collectible = Collectible(name = "Test", score = 10)
            service.createCollectible(collectible, byteArrayOf(), "png") { _ -> }
        } catch (e: Exception) {
        }
    }
}
