package com.example.bondoman

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.bondoman.service.TokenExpirationService
import com.example.bondoman.storage.TokenManager

class AppActivity : AppCompatActivity() {

    private val interval = 3000L // 30 secs in milliseconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)
        supportActionBar?.hide()

        val linearLayout = findViewById<LinearLayout>(R.id.app_linear)
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_in_from_bottom)
        linearLayout.startAnimation(animation)

        Handler(Looper.getMainLooper()).postDelayed({
            val isValid = TokenManager.isTokenValid(this@AppActivity)
            if (isValid) {
                // Redirect to main activity if token is valid
                startTokenExpirationService()
                startActivity(Intent(this@AppActivity, MainActivity::class.java))
            } else {
                // Redirect to login activity if token is not valid
                startActivity(Intent(this@AppActivity, LoginActivity::class.java))
            }

            finish() // Finish current activity to prevent going back to it using back button
        }, interval)
    }

    private fun startTokenExpirationService() {
        val intent = Intent(this, TokenExpirationService::class.java)
        startService(intent)
    }
}
