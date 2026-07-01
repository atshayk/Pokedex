package com.poke.dex.core.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

fun Context.setupBackgroundSync() {
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val syncRequest = PeriodicWorkRequestBuilder<PokemonSyncWorker>(24, TimeUnit.HOURS)
        .setConstraints(constraints)
        .build()

    WorkManager.getInstance(this).enqueueUniquePeriodicWork(
        "PokemonDatabaseSyncWork",
        ExistingPeriodicWorkPolicy.KEEP,
        syncRequest
    )
}