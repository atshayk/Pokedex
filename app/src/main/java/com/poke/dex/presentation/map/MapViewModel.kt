package com.poke.dex.presentation.map

import android.annotation.SuppressLint
import android.os.Looper
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.net.PlacesClient
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@HiltViewModel
class MapViewModel @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val placesClient: PlacesClient
): ViewModel() {
    private val _userLocation = MutableStateFlow<LatLng?>(null)
    val userLocation: StateFlow<LatLng?> = _userLocation.asStateFlow()

    private var locationCallback: LocationCallback? = null
    private var isListeningForUpdates = false

    private val _isMeasuringMode = MutableStateFlow(false)
    val isMeasuringMode: StateFlow<Boolean> = _isMeasuringMode.asStateFlow()

    private val _rulerStart = MutableStateFlow(LatLng(0.0, 0.0))
    val rulerStart: StateFlow<LatLng> = _rulerStart.asStateFlow()

    private val _rulerEnd = MutableStateFlow(LatLng(0.0, 0.0))
    val rulerEnd: StateFlow<LatLng> = _rulerEnd.asStateFlow()

    private val _calculatedDistanceKm = MutableStateFlow(0.0)
    val calculatedDistanceKm: StateFlow<Double> = _calculatedDistanceKm.asStateFlow()

    fun toggleMeasuringMode(mapCenter: LatLng) {
        val nextState = !_isMeasuringMode.value
        _isMeasuringMode.value = nextState
        if (nextState) {
            updateRulerPositions(
                start = LatLng(mapCenter.latitude - 0.002, mapCenter.longitude - 0.002),
                end = LatLng(mapCenter.latitude + 0.002, mapCenter.longitude + 0.002)
            )
        }
    }

    fun updateRulerPositions(start: LatLng? = null, end: LatLng? = null) {
        start?.let { _rulerStart.value = it }
        end?.let { _rulerEnd.value = it }
        _calculatedDistanceKm.value = calculateDistanceInKm(_rulerStart.value, _rulerEnd.value)
    }

    // Mathematical formula encapsulated inside the business domain logic layer
    private fun calculateDistanceInKm(start: LatLng, end: LatLng): Double {
        val radiusOfEarthKm = 6371.0
        val latDistance = Math.toRadians(end.latitude - start.latitude)
        val lngDistance = Math.toRadians(end.longitude - start.longitude)
        val a = sin(latDistance / 2) * sin(latDistance / 2) +
                cos(Math.toRadians(start.latitude)) * cos(Math.toRadians(end.latitude)) *
                sin(lngDistance / 2) * sin(lngDistance / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return radiusOfEarthKm * c
    }

    @SuppressLint("MissingPermission")
    fun fetchCachedOrLiveLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                _userLocation.value = LatLng(location.latitude, location.longitude)
            } else {
                startLocationUpdates()
            }
        }.addOnFailureListener {
            startLocationUpdates()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (isListeningForUpdates) return

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateIntervalMillis(2000)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (location in result.locations) {
                    if (location != null) {
                        _userLocation.value = LatLng(location.latitude, location.longitude)
                        stopLocationUpdates()
                        break
                    }
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
        isListeningForUpdates = true
    }

    fun stopLocationUpdates() {
        if (isListeningForUpdates && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback!!)
            isListeningForUpdates = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopLocationUpdates()
    }
}
