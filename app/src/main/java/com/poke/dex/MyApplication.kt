package com.poke.dex

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import androidx.work.WorkManager
import com.google.android.libraries.places.api.Places
import com.poke.dex.core.AppLifecycleObserver
import com.poke.dex.core.worker.setupBackgroundSync
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() {
            android.util.Log.d("MyApplication", "getWorkManagerConfiguration called")
            return Configuration.Builder()
                .setWorkerFactory(workerFactory)
                .build()
        }

    override fun onCreate() {
        super.onCreate()
        android.util.Log.d("MyApplication", "onCreate started")

        if (!Places.isInitialized()) {
            // Paste your actual Google Maps API string token right here
            Places.initialize(applicationContext, BuildConfig.MAPS_API_KEY, Locale.US)
        }

        if (::workerFactory.isInitialized) {
            android.util.Log.d("MyApplication", "HiltWorkerFactory successfully injected.")
            // Explicitly initialize WorkManager to ensure our configuration is used
            try {
                WorkManager.initialize(this, workManagerConfiguration)
                android.util.Log.d("MyApplication", "WorkManager manually initialized.")
            } catch (e: IllegalStateException) {
                android.util.Log.e("MyApplication", "WorkManager already initialized! Default config might be in use.", e)
            }
        } else {
            android.util.Log.e("MyApplication", "HiltWorkerFactory injection FAILED.")
        }

        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        setupBackgroundSync()
    }

    companion object {
        val appLifecycleObserver = AppLifecycleObserver()
    }
}