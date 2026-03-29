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

/**
 * LauncherActivity serves as the entry point of the application.
 * It is responsible for checking the user's authentication state
 * and routing them to either the Login screen or the Main application dashboard.
 */
class LauncherActivity : AppCompatActivity() {

    // Manager for handling session verification and authentication state.
    private lateinit var authManager: AuthManager

    /**
     * Launcher for the LoginActivity.
     * If the login is successful, re-checks the session to proceed to the home screen.
     * Otherwise, closes the application.
     */
    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            checkSession()
        } else {
            // Close app if login is cancelled or fails
            finish()
        }
    }


    /**
     * Initializes the launcher activity and begins the session check process.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        authManager = AuthManager.getInstance(this)
        checkSession()
    }

    /**
     * Verifies if there is an active user session.
     * Routes to the home screen on success, or opens the login screen on failure.
     */
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

    /**
     * Starts the LoginActivity using the registered activity result launcher.
     */
    private fun openLogin() {
        loginLauncher.launch(Intent(this, LoginActivity::class.java))
    }

    /**
     * Navigates the user to the MainActivity and clears the launcher from the backstack.
     * 
     * @param user The authenticated user object containing profile and role information.
     */
    private fun routeToHome(user: User) {
        Log.d("LauncherActivity", "Authenticated user: ${user.id}, role: ${user.role}")
        val destination = MainActivity::class.java
        startActivity(Intent(this, destination))
        finish()
    }
}