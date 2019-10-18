package com.relic.scheduler

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import timber.log.Timber
import javax.inject.Inject

class RelicWorkerFactory @Inject constructor(
  private val postRepository : PostRepository,
  private val commentRepo: CommentRepository
) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
        Timber.d(postRepository.toString())


        return when(workerClassName) {
            PostSyncWorker::class.java.name -> PostSyncWorker(appContext, workerParameters, postRepository, commentRepo)
            else -> null
        }
    }
}