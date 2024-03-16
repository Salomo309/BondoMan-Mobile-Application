package com.example.bondoman.room

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(milliseconds: Long?): Date? {
        return milliseconds?.let { Date(it) }
    }
}
