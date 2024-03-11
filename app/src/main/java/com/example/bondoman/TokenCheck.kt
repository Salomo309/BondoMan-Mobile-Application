package com.example.bondoman

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.*
import com.example.bondoman.service.RetrofitClient
import java.util.*

class TokenExpirationCheckService : Service() {

    private var running = false
    private var job: Job? = null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        running = true
        startExpirationCheck()
        return START_STICKY
    }

    override fun onDestroy() {
        running = false
        job?.cancel() // Cancel the coroutine job when the service is destroyed
        super.onDestroy()
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun startExpirationCheck() {
        job = GlobalScope.launch(Dispatchers.IO) {
            while (running) {
                val expirationTime = fetchTokenExpirationTime()
                if (expirationTime != null) {
                    val isTokenExpired = isTokenExpired(expirationTime)
                    if (isTokenExpired) {
                        handleExpiredToken()
                        break
                    }
                } else {
                    handleExpiredToken()
                    break
                }
                // Check every minute
                delay(60_000)
            }
        }
    }

    private suspend fun fetchTokenExpirationTime(): Long? {
        val token = TokenManager.getToken(applicationContext)
        if (token == null) {
            Log.e("TokenCheck", "Token not found")
            return null
        }

        return try {
            val authToken = "Bearer $token"
            val response = RetrofitClient.apiService.getTokenExpirationTime(authToken)
            print(response)
            response.exp
        } catch (e: Exception) {
            Log.e("TokenCheck", "Failed to fetch token expiration time: ${e.message}")
            null
        }
    }

    private fun isTokenExpired(expirationTime: Long): Boolean {
        val currentTime = Date().time / 1000 // Current time in seconds
        return expirationTime <= currentTime
    }

    private fun handleExpiredToken() {
        // Perform logout action
        TokenManager.saveToken(applicationContext, "")
        val intent = Intent(applicationContext, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        stopSelf() // Stop the service
    }
}