package com.example.bondoman.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.util.Date

@Entity(tableName = "transaction", primaryKeys = ["id"])
data class TransactionEntity (
    @ColumnInfo(name = "id")
    var id: Long,

    @ColumnInfo(name = "title")
    var title: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "amount")
    val amount: Int,

    @ColumnInfo(name = "location")
    val location: String,

    @ColumnInfo(name = "date")
    val date: Date = Date(), // Default to current date
)