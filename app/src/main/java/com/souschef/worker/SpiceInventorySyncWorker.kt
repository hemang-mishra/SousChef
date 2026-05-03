package com.souschef.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.souschef.notifications.RefillNotificationHelper
import com.souschef.service.device.DevicePreferenceService
import kotlinx.coroutines.flow.first
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit

/**
 * Background worker that periodically inspects compartment inventory and posts
 * a local refill alert for any compartment that has dropped below its threshold.
 *
 * Local-only — no FCM or remote pushes.
 */
class SpiceInventorySyncWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val deviceService: DevicePreferenceService by inject()

    override suspend fun doWork(): Result {
        return try {
            val compartments = deviceService.getCompartmentsFlow().first()
            compartments
                .filter { it.totalCapacityTsp > 0.0 && !it.isEmpty && it.isLowStock }
                .forEach { RefillNotificationHelper.notifyLowStock(applicationContext, it) }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "souschef_inventory_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SpiceInventorySyncWorker>(
                6, TimeUnit.HOURS
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
