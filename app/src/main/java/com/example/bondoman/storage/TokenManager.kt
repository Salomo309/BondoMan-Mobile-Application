package com.example.bondoman.storage

import android.content.Context
import android.util.Log
import com.example.bondoman.service.RetrofitClient
import java.util.*

object TokenManager {
    private const val TOKEN_KEY = "TOKEN_KEY"

    fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    private fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    suspend fun isTokenValid(context: Context): Boolean {
        val token = getToken(context)
        if (token != null) {
            val expirationTime = fetchTokenExpirationTime(context)
            if (expirationTime != null) {
                val currentTime = Date().time / 1000
                return expirationTime > currentTime
            }
        }

        return false
    }

    private suspend fun fetchTokenExpirationTime(context: Context): Long? {
        val token = getToken(context)
        if (token == null) {
            Log.e("TokenManager", "Token not found")
            return null
        }

        return try {
            val authToken = "Bearer $token"
            val response = RetrofitClient.apiService.getTokenExpirationTime(authToken)
            response.exp
        } catch (e: Exception) {
            Log.e("TokenManager", "Failed to fetch token expiration time: ${e.message}")
            null
        }
    }
}
