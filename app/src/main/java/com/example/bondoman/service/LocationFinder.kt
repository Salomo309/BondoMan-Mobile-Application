package com.example.bondoman.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient

class LocationFinder(private val context : Context,
                     private val activity : FragmentActivity) {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    fun getDeviceLocation(fusedLocationClient : FusedLocationProviderClient, callback: (Double, Double, String) -> Unit) {
        if (checkLocationPermission()) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val geocoder = Geocoder(context)
                        val addresses =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if (!addresses.isNullOrEmpty()) {

                            // Get location
                            val address = addresses[0] // Mengambil objek Address pertama dari daftar alamat

                            val subAdmin = address.subAdminArea // Kota
                            val locality = address.locality // Kecamatan
                            val thoroughfare = address.thoroughfare // Jalan

                            val addr = "$thoroughfare, $locality, $subAdmin"

                            // Call callback with obtained values
                            callback.invoke(location.latitude, location.longitude, addr)
                        } else {
                            Toast.makeText(context, "Failed to get address", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to get location", Toast.LENGTH_SHORT).show()
                    }
                }

                .addOnFailureListener { e ->
                    Toast.makeText(
                        context,
                        "Failed to get location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}