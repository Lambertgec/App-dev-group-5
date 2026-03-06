package com.group5.gue.ui.login.launcher

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.group5.gue.AdminMainActivity
import com.group5.gue.MainActivity
import com.group5.gue.api.SupabaseProvider
import com.group5.gue.data.model.Role
import com.group5.gue.data.user.UserRepository
import com.group5.gue.ui.login.LoginActivity
import io.github.jan.supabase.auth.auth

class LauncherActivity : AppCompatActivity() {

    private val userRepository = UserRepository.getInstance()

    private val loginLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            fetchProfileAndRoute()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkSession()
    }

    private fun checkSession() {
        val session = SupabaseProvider.supabaseClient.auth.currentSessionOrNull()
        if (session?.user != null) {
            fetchProfileAndRoute()
        } else {
            loginLauncher.launch(Intent(this, LoginActivity::class.java))
        }
    }

    private fun fetchProfileAndRoute() {
        val userId = SupabaseProvider.supabaseClient.auth.currentSessionOrNull()?.user?.id
        if (userId == null) {
            loginLauncher.launch(Intent(this, LoginActivity::class.java))
            return
        }

        userRepository.fetchAndCacheUser(userId) { user ->
            Log.d("LauncherActivity", "User fetched: $user")
            if (user != null && user.role == Role.ADMIN) {
                startActivity(Intent(this, AdminMainActivity::class.java))
            } else {
                startActivity(Intent(this, MainActivity::class.java))
            }
            finish()
        }
    }
}