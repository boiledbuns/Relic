package com.relic.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber

private const val KEY_POST_SOURCE = "KEY_POST_SOURCE"
private const val KEY_POST_PAGES_TO_SYNC = "KEY_POST_SOURCE"
private const val KEY_ENABLE_COMMENT_SYNC = "KEY_POST_SOURCE"

class PostSyncWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        workerParams.inputData.getString(KEY_POST_SOURCE)
        workerParams.inputData.getInt(KEY_POST_PAGES_TO_SYNC, 0)
        workerParams.inputData.getBoolean(KEY_ENABLE_COMMENT_SYNC, false)

        return Result.success()
    }

    companion object {
        fun createData(
          postSource : String,
          pageSyncCount : Int,
          enableCommentSync : Boolean
        ) : Data {
            return Data.Builder()
              .putString(KEY_POST_SOURCE, postSource)
              .putInt(KEY_POST_PAGES_TO_SYNC, pageSyncCount)
              .putBoolean(KEY_ENABLE_COMMENT_SYNC, enableCommentSync)
              .build()
        }
    }
}