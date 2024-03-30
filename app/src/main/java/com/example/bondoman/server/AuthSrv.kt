package com.example.bondoman.server

import com.example.bondoman.models.AuthRequest
import com.example.bondoman.models.AuthResponse
import com.example.bondoman.models.TokenResponse
import retrofit2.http.*

interface AuthService {
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: AuthRequest): AuthResponse

    @POST("/api/auth/token")
    suspend fun getTokenExpirationTime(@Header("Authorization") token: String): TokenResponse
}