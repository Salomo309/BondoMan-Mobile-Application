package com.example.bondoman.repository

import com.example.bondoman.models.AuthRequest
import com.example.bondoman.models.ItemsList
import com.example.bondoman.server.RetrofitClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class Repository {
    // Doing Login
    suspend fun login(email: String, password: String): String {
        val loginRequest = AuthRequest(email, password)
        val response = RetrofitClient.authService.login(loginRequest)
        return response.token
    }

    // Upload image captured
    suspend fun upload(file: ByteArray, token: String): ItemsList {
        val fileBody = file.toRequestBody("image/*".toMediaTypeOrNull())
        val response = RetrofitClient.fileService.upload("Bearer $token",
            MultipartBody.Part.createFormData("file", "image.jpeg", fileBody))
        return response.items
    }
}