package com.group5.gue.ui.login.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.group5.gue.AdminMainActivity
import com.group5.gue.MainActivity
import com.group5.gue.data.auth.AuthManager
import com.group5.gue.data.model.Role
import com.group5.gue.data.user.UserRepository
import com.group5.gue.ui.login.LoginActivity

class LauncherActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager
    private val userRepository = UserRepository.getInstance()

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            checkSession()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(this)
        checkSession()
    }

    private fun checkSession() {
        authManager.getCachedUserId { userId ->
            if (userId != null) {
                fetchProfileAndRoute(userId)
            } else {
                loginLauncher.launch(Intent(this, LoginActivity::class.java))
            }
        }
    }

    private fun fetchProfileAndRoute(userId: String) {
        userRepository.fetchAndCacheUser(userId) { user ->
            Log.d("LauncherActivity", "User fetched: $user for userId: $userId")
            if (user != null) {
                if(user.isAdmin) {
                    startActivity(Intent(this, AdminMainActivity::class.java))
                } else {
                    startActivity(Intent(this, MainActivity::class.java))
                }
                finish()
            } else {
                loginLauncher.launch(Intent(this, LoginActivity::class.java))
            }
        }
    }
}