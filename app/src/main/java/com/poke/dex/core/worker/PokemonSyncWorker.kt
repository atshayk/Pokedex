package com.poke.dex.core.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.poke.dex.domain.repository.PokemonRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class PokemonSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: PokemonRepository
    ) : CoroutineWorker(context, workerParams){

    override suspend fun doWork(): Result {
        Log.d("PokemonSyncWorker", "Hilt background sync started successfully.")

        try {
            repository.refreshPokemonList()
            Log.d("PokemonSyncWorker", "Hilt background sync completed clean.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("PokemonSyncWorker", "Hilt background sync failed: ${e.localizedMessage}")
            return Result.retry()
        }
    }
}