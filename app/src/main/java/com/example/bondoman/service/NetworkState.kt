package com.example.bondoman.service

import android.app.Service
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.os.Binder
import android.os.IBinder

class NetworkStateService : Service() {

    private val binder = LocalBinder()
    private lateinit var connectivityManager: ConnectivityManager

    inner class LocalBinder : Binder() {
        fun getService() : NetworkStateService {
            return this@NetworkStateService
        }
    }

    companion object {
        const val ACTION_NETWORK_STATE_CHANGE = "com.example.networkstatechange"
        const val EXTRA_NETWORK_STATE = "extra_network_state"
    }

    override fun onCreate() {
        super.onCreate()
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        registerNetworkCallback()
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun broadcastNetworkState(isConnected : Boolean) {
        val intent = Intent(ACTION_NETWORK_STATE_CHANGE)
        intent.putExtra(EXTRA_NETWORK_STATE, isConnected)
        sendBroadcast(intent)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            broadcastNetworkState(true)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            broadcastNetworkState(false)
        }
    }

    private fun registerNetworkCallback() {
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}