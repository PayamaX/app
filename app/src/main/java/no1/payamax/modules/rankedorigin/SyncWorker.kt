package no1.payamax.modules.rankedorigin

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import no1.payamax.api.infrastructure.ClientException
import no1.payamax.api.infrastructure.ServerException
import no1.payamax.config.PaxConfig
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {
    private val rankedOrigins: RankedOriginContract by inject()

    override suspend fun doWork(): Result {
        return try {
            rankedOrigins.sync()
            Result.success()
        } catch (e: IOException) {
            Log.e("SyncWorker", "Exception while syncing data", e)
            Result.retry()
        } catch (e: ClientException) {
            Log.e("SyncWorker", "Exception while syncing data", e)
            Result.retry()
        } catch (e: ServerException) {
            Log.e("SyncWorker", "Exception while syncing data", e)
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "ranked-origin-sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(
                PaxConfig.RANKED_ORIGINS_AGE_MILLIS,
                TimeUnit.MILLISECONDS,
            ).setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}
