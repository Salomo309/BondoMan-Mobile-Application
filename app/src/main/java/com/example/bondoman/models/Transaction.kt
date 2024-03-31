package com.example.bondoman.models

import android.os.Parcel
import android.os.Parcelable
import java.util.*

data class Transaction(
    val id: Long,
    var title: String,
    val nim: String,
    val category: String,
    var amount: Double,
    var location: String,
    var longitude: Double,
    var latitude: Double,
    val date: Date
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        Date(parcel.readLong())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeString(title)
        parcel.writeString(nim)
        parcel.writeString(category)
        parcel.writeDouble(amount)
        parcel.writeString(location)
        parcel.writeDouble(longitude)
        parcel.writeDouble(latitude)
        parcel.writeLong(date.time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}

data class TransactionSummary(
    val category: String,
    val totalAmount: Double
)
