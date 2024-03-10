package com.example.bondoman.models

data class AuthRequest (
    val email: String,
    val password: String,
)

data class AuthResponse (
    val token: String
)