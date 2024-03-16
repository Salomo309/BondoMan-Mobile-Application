package com.example.bondoman.models

import com.google.gson.annotations.SerializedName

data class Item(
    val name: String,
    val qty: Int,
    val price: Double
)

data class UploadResponse(
    @SerializedName("items")
    val items: ItemsList
)

data class ItemsList(
    @SerializedName("items")
    val items: List<Item>
)