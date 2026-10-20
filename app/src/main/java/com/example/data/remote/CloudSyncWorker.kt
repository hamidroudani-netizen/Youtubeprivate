package com.example.data.remote

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.data.local.AppDatabase
import com.example.data.local.ParentPreferences
import java.util.concurrent.TimeUnit

class CloudSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val prefs = ParentPreferences(applicationContext)
        val familyCode = prefs.getFamilyCode()

        if (familyCode.isBlank()) {
            Log.d(TAG, "No family code set, skipping sync worker execution.")
            return Result.success()
        }

        Log.d(TAG, "CloudSyncWorker executing sync for family code: $familyCode (Run attempt: $runAttemptCount)")

        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val syncManager = FirestoreSyncManager(
                context = applicationContext,
                videoDao = db.videoDao(),
                profileDao = db.profileDao(),
                syncQueueDao = db.syncQueueDao()
            )

            val success = syncManager.processSyncQueueAndSync(familyCode)

            if (success) {
                Log.d(TAG, "CloudSyncWorker completed successfully.")
                Result.success()
            } else {
                Log.w(TAG, "CloudSyncWorker sync failed or network offline, requesting retry.")
                if (runAttemptCount < MAX_RETRIES) {
                    Result.retry()
                } else {
                    Log.e(TAG, "CloudSyncWorker reached max retries ($MAX_RETRIES). Failing task.")
                    Result.failure()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during CloudSyncWorker execution", e)
            if (runAttemptCount < MAX_RETRIES) Result.retry() else Result.failure()
        }
    }

    companion object {
        private const val TAG = "CloudSyncWorker"
        private const val WORK_NAME_ONE_TIME = "CloudSyncOneTimeWork"
        private const val WORK_NAME_PERIODIC = "CloudSyncPeriodicWork"
        private const val MAX_RETRIES = 5

        fun enqueueOneTimeSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest = OneTimeWorkRequestBuilder<CloudSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                syncWorkRequest
            )
            Log.d(TAG, "Enqueued unique one-time CloudSyncWorker request.")
        }

        fun schedulePeriodicSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val periodicSyncRequest = PeriodicWorkRequestBuilder<CloudSyncWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30, TimeUnit.SECONDS
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicSyncRequest
            )
            Log.d(TAG, "Scheduled periodic CloudSyncWorker request.")
        }
    }
}
