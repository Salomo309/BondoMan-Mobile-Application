package com.example.bondoman

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            val isValid = checkTokenValidity()
            if (isValid) {
                // Redirect to main activity if token is valid
                startActivity(Intent(this@AppActivity, MainActivity::class.java))
            } else {
                // Redirect to login activity if token is not valid
                startActivity(Intent(this@AppActivity, LoginActivity::class.java))
            }
            finish() // Finish current activity to prevent going back to it using back button
        }
    }

    private suspend fun checkTokenValidity(): Boolean {
        return withContext(Dispatchers.IO) {
            TokenManager.isTokenValid(this@AppActivity)
        }
    }
}
