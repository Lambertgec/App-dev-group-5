package com.group5.gue.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class UserTest {

    @Test
    fun `user with isAdmin true should have ADMIN role`() {
        val user = User(id = "1", name = "Admin", isAdmin = true)
        assertEquals(Role.ADMIN, user.role)
    }

    @Test
    fun `user with isAdmin false should have USER role`() {
        val user = User(id = "2", name = "User", isAdmin = false)
        assertEquals(Role.USER, user.role)
    }

    @Test
    fun `user default values`() {
        val user = User()
        assertEquals("", user.id)
        assertEquals(null, user.name)
        assertEquals(0, user.score)
        assertEquals(false, user.isAdmin)
        assertEquals(Role.USER, user.role)
    }

    @Test
    fun `user equality and hashcode`() {
        val user1 = User(id = "1", name = "Test", score = 100, isAdmin = true)
        val user2 = User(id = "1", name = "Test", score = 100, isAdmin = true)
        val user3 = User(id = "2", name = "Test", score = 100, isAdmin = true)

        assertEquals(user1, user2)
        assertEquals(user1.hashCode(), user2.hashCode())
        assertNotEquals(user1, user3)
    }

    @Test
    fun `user copy should preserve fields`() {
        val user = User(id = "1", name = "Original", score = 10, isAdmin = false)
        val copied = user.copy(name = "New Name")

        assertEquals("1", copied.id)
        assertEquals("New Name", copied.name)
        assertEquals(10, copied.score)
        assertEquals(false, copied.isAdmin)
    }
}
