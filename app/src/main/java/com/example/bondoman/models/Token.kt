package com.example.bondoman.models

data class TokenResponse (
    val nim: String,
    val iat: Long,
    val exp: Long
)