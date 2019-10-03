package com.relic.scheduler

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class ScheduleManager(
    appContext : Context
) {

    private val workManager = WorkManager.getInstance(appContext)

    fun setupPostSync() {
        val postSyncReq = PeriodicWorkRequestBuilder<PostSyncWorker>(1, TimeUnit.DAYS)
            .build()
        workManager.enqueue(postSyncReq)
    }
}