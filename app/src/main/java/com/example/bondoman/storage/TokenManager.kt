package com.example.bondoman.storage

import android.content.Context

object TokenManager {
    private const val TOKEN_KEY = "TOKEN_KEY"

    fun saveToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(TOKEN_KEY, token)
        editor.apply()
    }

    fun getToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString(TOKEN_KEY, null)
    }
}
