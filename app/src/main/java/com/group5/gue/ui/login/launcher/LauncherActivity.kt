package com.group5.gue.ui.login.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.group5.gue.AdminMainActivity
import com.group5.gue.MainActivity
import com.group5.gue.data.Result
import com.group5.gue.data.auth.AuthManager
import com.group5.gue.data.model.Role
import com.group5.gue.data.model.User
import com.group5.gue.ui.login.LoginActivity

// Handles access flow to the app and authentication checking

class LauncherActivity : AppCompatActivity() {

    private lateinit var authManager: AuthManager

    // Launcher for login activity results
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

    // Checks if there is an active session and routes to the appropriate screen, otherwise opens login
    private fun checkSession() {
        authManager.getUserFromSession { result ->
            when (result) {
                is Result.Success -> routeToHome(result.data)
                is Result.Error -> {
                    Log.d("LauncherActivity", "No active session, launching login",
                        result.error)
                    openLogin()
                }
            }
        }
    }

    private fun openLogin() {
        loginLauncher.launch(Intent(this, LoginActivity::class.java))
    }

    // Routes user to the appropriate home screen based on their role
    private fun routeToHome(user: User) {
        Log.d("LauncherActivity", "Authenticated user: ${user.id}, role: ${user.role}")
        val destination = MainActivity::class.java
        startActivity(Intent(this, destination))
        finish()
    }
}