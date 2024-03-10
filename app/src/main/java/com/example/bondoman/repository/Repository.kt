package com.example.bondoman.repository

import com.example.bondoman.models.AuthRequest
import com.example.bondoman.service.RetrofitClient

class Repository {
    suspend fun login(email: String, password: String): String {
        val loginRequest = AuthRequest(email, password)
        val response = RetrofitClient.apiService.login(loginRequest)
        return response.token
    }
}