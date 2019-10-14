package com.relic.scheduler

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.relic.data.PostSource
import java.util.*
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

    private val days = arrayOf(SyncRepeatDays.SUNDAY, SyncRepeatDays.MONDAY, SyncRepeatDays.TUESDAY, SyncRepeatDays.WEDNESDAY, SyncRepeatDays.THURSDAY, SyncRepeatDays.FRIDAY, SyncRepeatDays.SATURDAY)
    private val workManager = WorkManager.getInstance(app)

    override fun setupPostSync(
      postSource: PostSource,
      pagesToSync: Int,
      timeToSync: Int,
      repeatType: SyncRepeatOption,
      repeatDay: SyncRepeatDays,
      commentSyncEnabled: Boolean
    ) {
        val constraints = Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .build()

        val repeatInterval = when(repeatType) {
            SyncRepeatOption.DAILY -> 1L
            SyncRepeatOption.WEEKLY -> 7L
        }

        // time duration left between current # of seconds and # of seconds until schedule
        val durationUntilSync = calculateMinutesFromNextSync(timeToSync, repeatType, repeatDay)
        val postSyncWorkerData = PostSyncWorker.createData(postSource.getSourceName(), pagesToSync, commentSyncEnabled)
        val postSyncReq = PeriodicWorkRequestBuilder<PostSyncWorker>(repeatInterval, TimeUnit.DAYS)
          .setInputData(postSyncWorkerData)
          .setConstraints(constraints)
          .setInitialDelay(durationUntilSync, TimeUnit.MINUTES)
          .build()

        workManager.enqueueUniquePeriodicWork(postSource.getSourceName(), ExistingPeriodicWorkPolicy.REPLACE, postSyncReq)
    }

    override fun cancelPostSync(postSource: PostSource) {
        workManager.cancelUniqueWork(postSource.getSourceName())
    }

    private fun getDayOfWeek(day : SyncRepeatDays) : Int {
        return days.indexOf(day)
    }

    private fun calculateMinutesFromNextSync(
      timeToSync : Int,
      repeatType: SyncRepeatOption,
      repeatDay: SyncRepeatDays
    ) : Long {
        val now = Calendar.getInstance()
        return when(repeatType) {
            SyncRepeatOption.WEEKLY -> {
                val syncDay = getDayOfWeek(repeatDay)
                // get time within context of week in minutes
                val syncTime = syncDay*24*60 + timeToSync
                val currentTime = now.get(Calendar.DATE)*24*60L + now.get(Calendar.HOUR)*60 + now.get(Calendar.MINUTE)

                // modify duration until sync for next week if sync day is already passed
                if (syncTime < currentTime) {
                    // minutes for a full next week - difference
                    7*24*60 - (currentTime - syncTime)
                } else {
                    // number of minutes to delay from now until scheduled sync time
                    syncTime - currentTime
                }
            }
            SyncRepeatOption.DAILY -> {
                // get time within context of week in minutes
                val currentTime = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE)

                // modify duration until sync for next day if sync day is already passed
                if (timeToSync < currentTime) {
                    // minutes for a full day - difference
                    (24*60 - (currentTime - timeToSync)).toLong()
                } else {
                    // number of minutes to delay from now until scheduled sync time
                    (timeToSync - currentTime).toLong()
                }
            }
        }

    }
}