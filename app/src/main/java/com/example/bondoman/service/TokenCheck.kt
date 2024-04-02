package com.example.bondoman.service

import android.app.*
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.example.bondoman.LoginActivity
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.*

class TokenExpirationService : Service() {

    private val interval = 10000L // 10 secs in milliseconds
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        Log.d("TokenExpirationService", "Service created")
    }

    override fun onBind(intent: Intent?): IBinder? {
        // nothing to bind
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TokenExpirationService", "Service started")
        startBackgroundTask()
        return START_STICKY // If the service is killed, it will be automatically restarted
    }

    private fun startBackgroundTask() {
        coroutineScope.launch {
            while (true) {
                try {
                    if (!TokenManager.isTokenValid(this@TokenExpirationService)) {
                        Log.w("TokenCheck", "Token expired, stopping service and potentially navigating to login")
                        setOff()
                        break
                    }
                } catch (e: Exception) {
                    setOff()
                }
                delay(interval)
            }
        }
    }

    private fun setOff() {
        log("Service stopped")
        stopSelf()
        navigateToLoginPage()
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
    }

    override fun onDestroy() {
        log("Service destroyed")
        super.onDestroy()
        coroutineScope.cancel()
    }

    private fun log(str: String) {
        Log.d("TokenExpirationService", "log: $str")
    }
}
