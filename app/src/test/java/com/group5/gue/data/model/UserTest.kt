package com.group5.gue.data.model

import org.junit.Assert.assertEquals
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
}
