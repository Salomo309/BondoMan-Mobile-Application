package com.example.bondoman.service

import com.example.bondoman.models.AuthRequest
import com.example.bondoman.models.AuthResponse
import retrofit2.http.*

interface AuthService {
    @POST("/api/auth/login")
    suspend fun login(@Body loginRequest: AuthRequest): AuthResponse
}