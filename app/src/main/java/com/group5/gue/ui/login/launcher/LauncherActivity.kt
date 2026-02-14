package com.group5.gue.ui.login.launcher

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.group5.gue.AdminMainActivity
import com.group5.gue.MainActivity
import com.group5.gue.data.auth.AuthRepository
import com.group5.gue.data.model.Role
import com.group5.gue.ui.login.LoginActivity

class LauncherActivity : AppCompatActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authRepository = AuthRepository.getInstance(this)
        routeUser()
    }

    private fun routeUser() {
        authRepository.resolveSession { userId ->
            if (userId == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                return@resolveSession
            }
            val role = authRepository.getCachedUser()?.getRole() ?: Role.USER
            if (role == Role.ADMIN) {
                startActivity(Intent(this, AdminMainActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
    }
}