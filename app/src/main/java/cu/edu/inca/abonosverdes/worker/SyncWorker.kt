package cu.edu.inca.abonosverdes.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import cu.edu.inca.abonosverdes.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncRepository: SyncRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val result = syncRepository.sync()
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "SyncWorker"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                // Removed network constraint to allow pre-population of sample data offline
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                syncRequest
            )
        }
    }
}
