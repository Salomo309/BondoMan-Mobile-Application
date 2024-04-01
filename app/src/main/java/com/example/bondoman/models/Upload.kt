package com.example.bondoman.models

import com.google.gson.annotations.SerializedName

data class Item(
    var name: String,
    val qty: Int,
    var price: Double
)

data class UploadResponse(
    @SerializedName("items")
    val items: ItemsList
)

data class ItemsList(
    @SerializedName("items")
    val items: List<Item>
)