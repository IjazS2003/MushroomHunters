package com.example.mushroomhunters.helper

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*

class LocationHelper(private val context: Context) {

    private var fusedLocationProvider: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            locationResult.locations.forEach { location ->
                Log.d(TAG, "New location received: Latitude=${location.latitude}, Longitude=${location.longitude}")
            }
        }
    }

    fun startLocationUpdates() {
        if (!checkLocationPermissions()) {
            Log.e(TAG, "Cannot start location updates: Missing required permissions")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 10_000L
        ).setMinUpdateIntervalMillis(5_000L)
            .build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permissions not granted, cannot request updates")
            return
        }

        fusedLocationProvider.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.d(TAG, "Location updates started successfully")
    }

    fun stopLocationUpdates() {
        fusedLocationProvider.removeLocationUpdates(locationCallback)
        Log.d(TAG, "Location updates have been stopped")
    }

    fun getLastKnownLocation(onLocationResult: (Location?, String?, Double?, Double?) -> Unit) {
        if (!checkLocationPermissions()) {
            Log.e(TAG, "Cannot fetch last known location: Permissions are missing")
            onLocationResult(null, null, null, null)
            return
        }

        fusedLocationProvider.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val address = getAddressFromCoordinates(it.latitude, it.longitude)
                Log.d(TAG, "Last known location retrieved: Latitude=${it.latitude}, Longitude=${it.longitude}, Address=${address ?: "N/A"}")
                onLocationResult(it, address, it.latitude, it.longitude)
            } ?: run {
                Log.w(TAG, "No last known location found on the device")
                onLocationResult(null, null, null, null)
            }
        }.addOnFailureListener { e ->
            Log.e(TAG, "Failed to retrieve last known location: ${e.message}")
            onLocationResult(null, null, null, null)
        }
    }

    private fun checkLocationPermissions(): Boolean {
        val fineLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fineLocationPermission == PackageManager.PERMISSION_GRANTED || coarseLocationPermission == PackageManager.PERMISSION_GRANTED
    }

    private fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = Geocoder(context)
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses?.isNotEmpty() == true) {
                addresses[0]?.getAddressLine(0)
            } else {
                Log.w(TAG, "Could not resolve address for coordinates: Latitude=$latitude, Longitude=$longitude")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while resolving address: ${e.message}")
            null
        }
    }

    companion object {
        private const val TAG = "LocationHelper"
    }
}
