package com.relic.scheduler

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.relic.data.PostSource
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Workers can be identified through 2 ways
 * 1. For single subreddit sync - through their post source name
 * 2. For group subreddit sync - through their group names (TODO)
 */
class ScheduleManagerImpl @Inject constructor(
  app : Application
) : ScheduleManager {
    private val workManager = WorkManager.getInstance(app)

    override fun setupPostSync(postSource: PostSource, syncPages : Int, time : Long) {
        val constraints = Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .build()

        val postSyncReq = PeriodicWorkRequestBuilder<PostSyncWorker>(1, TimeUnit.DAYS)
          .setConstraints(constraints)
          .build()

        workManager.enqueueUniquePeriodicWork(postSource.getSourceName(), ExistingPeriodicWorkPolicy.REPLACE, postSyncReq)
    }

    override fun cancelPostSync(postSource: PostSource) {
        workManager.cancelUniqueWork(postSource.getSourceName())
    }
}