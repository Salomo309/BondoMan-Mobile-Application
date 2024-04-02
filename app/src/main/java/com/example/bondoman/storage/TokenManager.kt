package com.example.bondoman.storage

import android.content.Context
import android.util.Log
import com.example.bondoman.server.RetrofitClient
import java.util.*

object TokenManager {
    private const val TOKEN_KEY = "TOKEN_KEY"
    private const val EXP_DATE_KEY = "EXP_DATE_KEY"
    private const val NIM_KEY = "NIM_KEY"

    fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }

    private fun removeToken(context: Context) {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(TOKEN_KEY)
        editor.apply()
    }

    private fun saveExpDate(context: Context, expDate: Long) {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putLong(EXP_DATE_KEY, expDate)
        editor.apply()
    }

    private fun getExpDate(context: Context): Long {
        val expDate = 100L;
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        return sharedPreferences.getLong(EXP_DATE_KEY, expDate)
    }

    private fun removeExpDate(context: Context) {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(EXP_DATE_KEY)
        editor.apply()
    }

    private fun saveNIM(context: Context, nim: String) {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(NIM_KEY, nim)
        editor.apply()
    }

    fun getNIM(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        return sharedPreferences.getString(NIM_KEY, null)
    }

    private fun removeNIM(context: Context) {
        val sharedPreferences = context.getSharedPreferences("BondoMan", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(NIM_KEY)
        editor.apply()
    }

    fun removeSharedPrefs(context: Context) {
        removeToken(context)
        removeExpDate(context)
        removeNIM(context)
    }

    fun isTokenValid(context: Context): Boolean {
        val expirationTime = getExpDate(context)
        val currentTime = Date().time / 1000
        return expirationTime > currentTime
    }

    suspend fun fetchTokenResponse(context: Context, token: String) {
        try {
            val authToken = "Bearer $token"
            val response = RetrofitClient.authService.getTokenExpirationTime(authToken)
            saveExpDate(context, response.exp)
            saveNIM(context, response.nim)
        } catch (e: Exception) {
            Log.e("TokenManager", "Failed to fetch token response: ${e.message}")
        }
    }
}
