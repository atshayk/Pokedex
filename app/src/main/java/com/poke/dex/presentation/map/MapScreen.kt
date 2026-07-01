package com.poke.dex.presentation.map

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.core.net.toUri


@Composable
fun MapScreen(
    viewModel: MapViewModel = hiltViewModel(),
    onVerifyHardwareSettings: () -> Unit
) {
    val context = LocalContext.current
    val currentLocation by viewModel.userLocation.collectAsState()
    var mapType by remember { mutableStateOf(MapType.NORMAL) }
    var viewInExternalMap by remember { mutableStateOf(false) }
    val customMarkers = remember { mutableStateListOf<LatLng>() }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineGranted || coarseGranted) {
            onVerifyHardwareSettings()
        } else {
            Toast.makeText(context, "Location permission authorization denied.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (hasFine || hasCoarse) {
            onVerifyHardwareSettings()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (currentLocation != null) {
            if (!viewInExternalMap) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(currentLocation!!, 15f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(mapType = mapType),
                    onMapClick = { latLng ->
                        customMarkers.add(latLng)
                    }
                ) {
                    // Fixed User Location Pin
                    Marker(
                        state = MarkerState(currentLocation!!),
                        title = "Your Location",
                        snippet = "Lat: ${currentLocation!!.latitude}, Lng: ${currentLocation!!.longitude}"
                    )

                    // 2. DYNAMIC MARKERS LOOP
                    customMarkers.forEachIndexed { index, markerLatLng ->
                        Marker(
                            state = MarkerState(markerLatLng),
                            title = "Custom Waypoint #${index + 1}",
                            snippet = "Lat: ${markerLatLng.latitude}, Lng: ${markerLatLng.longitude}"
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Map opened in Google Maps app",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        Button(onClick = {
                            openGoogleMapsIntent(context, currentLocation!!)
                        }) {
                            Text("Relaunch Google Maps")
                        }
                    }
                }
            }

            // Controls Toolbar Container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View Mode Toggles
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewInExternalMap = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (!viewInExternalMap) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text("In-App")
                    }
                    Button(
                        onClick = {
                            viewInExternalMap = true
                            openGoogleMapsIntent(context, currentLocation!!)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (viewInExternalMap) MaterialTheme.colorScheme.primary else Color.Gray
                        )
                    ) {
                        Text("Google Maps")
                    }
                }

                // Layer Selection Styles (Only visible if using in-app view)
                if (!viewInExternalMap) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { mapType = MapType.NORMAL },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mapType == MapType.NORMAL) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text("Default")
                        }
                        Button(
                            onClick = { mapType = MapType.SATELLITE },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mapType == MapType.SATELLITE) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text("Satellite")
                        }
                        Button(
                            onClick = { mapType = MapType.HYBRID },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (mapType == MapType.HYBRID) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        ) {
                            Text("Hybrid")
                        }
                        Button(
                            onClick = { customMarkers.clear() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Clear Pins")
                        }
                    }
                }
            }
        } else {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            Text(
                text = "Resolving GPS coordinates...",
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
            )
        }
    }
}

private fun openGoogleMapsIntent(context: android.content.Context, location: LatLng) {
    val gmmIntentUri = "geo:${location.latitude},${location.longitude}?z=15".toUri()
    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
        setPackage("com.google.android.apps.maps")
    }
    if (mapIntent.resolveActivity(context.packageManager) != null) {
        context.startActivity(mapIntent)
    } else {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            "https://www.google.com/maps/@${location.latitude},${location.longitude},15z".toUri()
        )
        context.startActivity(browserIntent)
    }
}