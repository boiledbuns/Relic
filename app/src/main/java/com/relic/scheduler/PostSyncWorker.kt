package com.relic.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.relic.api.response.Listing
import com.relic.data.PostRepository
import com.relic.data.PostRepositoryImpl
import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.domain.models.PostModel
import timber.log.Timber
import javax.inject.Inject

private const val KEY_POST_SOURCE = "KEY_POST_SOURCE"
private const val KEY_POST_PAGES_TO_SYNC = "KEY_POST_SOURCE"
private const val KEY_ENABLE_COMMENT_SYNC = "KEY_POST_SOURCE"

class PostSyncWorker(
    private val appContext: Context,
    private val workerParams: WorkerParameters,
    private val postRepo: PostRepository
) : Worker(appContext, workerParams) {

    class Factory @Inject constructor(
      private val postRepository : PostRepository
    ) : WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
            Timber.d(postRepository.toString())
            return PostSyncWorker(appContext, workerParameters, postRepository)
        }
    }

    override fun doWork(): Result {
        Timber.d("do work ")
        val postSourceName = workerParams.inputData.getString(KEY_POST_SOURCE)
        val pagesToSync = workerParams.inputData.getInt(KEY_POST_PAGES_TO_SYNC, 0)
        val syncComments = workerParams.inputData.getBoolean(KEY_ENABLE_COMMENT_SYNC, false)

        val posts = ArrayList<PostModel>()

        var syncedPages = 0
        var next : String? = null
        while (syncedPages < pagesToSync ) {
            if (next == null && syncedPages == 0) {
                break
            }

//            val retrievedPosts = postRepo.retrieveSortedPosts(postSource: PostSource, sortType: SortType, sortScope: SortScope) : Listing<PostModel>

            retrievedPosts.data.children?.let {
                posts.addAll(it)
            }

             next = retrievedPosts.data.after
            syncedPages += 1
        }

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