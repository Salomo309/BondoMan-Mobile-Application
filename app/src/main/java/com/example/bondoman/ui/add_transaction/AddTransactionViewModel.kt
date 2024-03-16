package com.example.bondoman.ui.add_transaction

import androidx.lifecycle.ViewModel
import android.location.Location

class AddTransactionViewModel : ViewModel() {
    private var currentLocation: Location? = null

    fun setLocation(location: Location) {
        currentLocation = location
    }

    fun getCurrentLocation(): Location? {
        return currentLocation
    }
}