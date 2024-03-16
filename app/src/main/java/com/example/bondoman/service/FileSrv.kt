package com.example.bondoman.service

import com.example.bondoman.models.UploadResponse
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface FileService {
    @Multipart
    @POST("/api/bill/upload")
    suspend fun upload(@Header("Authorization") token: String, @Part file: MultipartBody.Part) : UploadResponse
}