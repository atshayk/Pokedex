package com.poke.dex.presentation.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.poke.dex.core.receiver.NetworkChangeReceiver
import com.poke.dex.core.service.MyFirebaseNotificationService
import com.poke.dex.presentation.pokedex.PokeDexScreen
import com.poke.dex.presentation.map.MapScreen
import com.poke.dex.presentation.profile.ProfileScreen
import com.poke.dex.ui.theme.PokeDexTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val foregroundNotificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra(MyFirebaseNotificationService.EXTRA_TITLE)
            val body = intent?.getStringExtra(MyFirebaseNotificationService.EXTRA_BODY)
            Toast.makeText(this@MainActivity, "In-App Update: $title\n$body", Toast.LENGTH_LONG).show()
        }
    }

    private lateinit var networkReceiver: NetworkChangeReceiver

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivityLifecycle", "onStart called - App is being created")

        networkReceiver = NetworkChangeReceiver { isOnline ->
            if (isOnline) {
                Toast.makeText(this, "Connected to the internet!", Toast.LENGTH_SHORT).show()
                Log.d("NetworkStatus", "App successfully connected to a network")
            } else {
                Toast.makeText(this, "Connection lost.", Toast.LENGTH_SHORT).show()
            }
        }

        enableEdgeToEdge()

        setContent {
            PokeDexTheme {
                val context = LocalContext.current
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(context, "Notification permission authorized!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Alerts disabled. You won't receive push updates.", Toast.LENGTH_LONG).show()
                    }
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // Call the tab selection panel directly
                MainDashboardView()
            }
        }
    }

    @Composable
    fun MainDashboardView() {
        var currentTab by remember { mutableIntStateOf(0) }
        val context = LocalContext.current

        Scaffold(
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = { Text("🐾", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Pokémon") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = {
                            currentTab = 0
                            Toast.makeText(context, "Tap 'Update Photo' in the PokeDex header!", Toast.LENGTH_LONG).show()
                        },
                        icon = { Text("📸", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Camera") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 2,
                        onClick = { currentTab = 2 },
                        icon = { Text("📍", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Map") }
                    )
                    NavigationBarItem(
                        selected = currentTab == 3,
                        onClick = { currentTab = 3 },
                        icon = { Text("👤", style = MaterialTheme.typography.titleMedium) },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (currentTab) {
                    0 -> PokeDexScreen()
                    2 -> MapScreen(onVerifyHardwareSettings = {
                        // Directly runs your verified system-level hardware checks
                        (context as? MainActivity)?.checkSystemHardwareSettingsFallback()
                    })
                    3 -> ProfileScreen()
                }
            }
        }
    }

    // Public bridge helper function for Compose screen callbacks
    fun checkSystemHardwareSettingsFallback() {
        val intent = Intent(this, com.poke.dex.presentation.map.MapActivity::class.java)
        startActivity(intent)
    }

    override fun onStart() {
        super.onStart()
        Log.d("MainActivityLifecycle", "onStart called - App is becoming visible")
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        Log.d("MainActivityLifecycle", "onResume called - App is active again")

        val filterConnectivity = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, filterConnectivity)

        val filterLocalBroadcast = IntentFilter(MyFirebaseNotificationService.ACTION_FOREGROUND_NOTIFICATION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(foregroundNotificationReceiver, filterLocalBroadcast, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(foregroundNotificationReceiver, filterLocalBroadcast)
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d("MainActivityLifecycle", "onPause called - UI is frozen")
        unregisterReceiver(networkReceiver)
        unregisterReceiver(foregroundNotificationReceiver)
    }

    override fun onStop() {
        super.onStop()
        Log.d("MainActivityLifecycle", "onStop called - App is hidden")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivityLifecycle", "onDestroy called - App is being completely closed")
    }
}
