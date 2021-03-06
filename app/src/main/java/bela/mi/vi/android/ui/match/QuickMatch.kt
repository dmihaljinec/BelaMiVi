package bela.mi.vi.android.ui.match

import android.content.Context
import androidx.hilt.Assisted
import androidx.hilt.work.WorkerInject
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import bela.mi.vi.interactor.WithMatch
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.TimeUnit

private const val TAG_QUICK_MATCH_CLEANER = "QuickMatchCleaner"

fun enqueueQuickMatchCleaner(context: Context) {
    val worker = PeriodicWorkRequestBuilder<QuickMatchCleaner>(
        24,
        TimeUnit.HOURS,
        2,
        TimeUnit.HOURS
    ).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        TAG_QUICK_MATCH_CLEANER,
        ExistingPeriodicWorkPolicy.KEEP,
        worker
    )
}

fun cancelQuickMatchCleaner(context: Context) {
    WorkManager.getInstance(context).cancelAllWorkByTag(TAG_QUICK_MATCH_CLEANER)
}

class QuickMatchCleaner @WorkerInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val withMatch: WithMatch
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = coroutineScope {
        withMatch.removeQuickMatches()
        Result.success()
    }
}
