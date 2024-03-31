package com.example.bondoman.service

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.bondoman.LoginActivity
import com.example.bondoman.R
import com.example.bondoman.storage.TokenManager
import kotlinx.coroutines.*

class TokenExpirationService : Service() {

    private val interval = 30000L // 30 secs in milliseconds
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val notificationChannelId = "token_expiration_channel"
    private val notificationId = 1

    override fun onCreate() {
        super.onCreate()
        Log.d("TokenExpirationService", "Service created");
    }

    override fun onBind(intent: Intent?): IBinder? {
        // nothing to bind
        return null
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("TokenExpirationService", "Service started")
        startBackgroundTask()
        startForeground(notificationId, createNotification())
        return START_STICKY // If the service is killed, it will be automatically restarted
    }

    private fun createNotification(): Notification {
        val channel = NotificationChannel(
            notificationChannelId,
            "Token Expiration Service",
            NotificationManager.IMPORTANCE_NONE
        )
        channel.description = "Background service checking token validity"

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Token Expiration Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.done_icon)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setAutoCancel(false) // Prevent automatic cancellation on swiping away

        return builder.build()
    }

    private fun startBackgroundTask() {
        coroutineScope.launch {
            while (true) {
                try {
                    if (!TokenManager.isTokenValid(this@TokenExpirationService)) {
                        Log.w("TokenCheck", "Token expired, stopping service and potentially navigating to login");
                        setOff();
                        break;
                    }
                    log("30 secs checking");
                } catch (e: Exception) {
                    log("Error checking token: $e");
                    setOff();
                }
                delay(interval);
            }
        }
    }

    private fun setOff() {
        log("Service stopped");
        stopSelf();
        navigateToLoginPage();
    }

    private fun navigateToLoginPage() {
        val intent = Intent(this, LoginActivity::class.java);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    override fun onDestroy() {
        log("Service destroyed");
        super.onDestroy();
        coroutineScope.cancel();
    }

    private fun log(str: String) {
        Log.d("TokenExpirationService", "log: $str")
    }
}
