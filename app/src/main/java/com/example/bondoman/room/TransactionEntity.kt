package com.example.bondoman.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.Date

@Entity(tableName = "transactions", primaryKeys = ["id", "nim"])
data class TransactionEntity (
    @ColumnInfo(name = "id")
    var id: Long,

    @ColumnInfo(name = "nim")
    var nim: String,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "amount")
    val amount: Double,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "longitude")
    val longitude: Double,

    @ColumnInfo(name = "latitude")
    val latitude: Double,

    @ColumnInfo(name = "date")
    val date: Date = Date(), // Default to current date
)