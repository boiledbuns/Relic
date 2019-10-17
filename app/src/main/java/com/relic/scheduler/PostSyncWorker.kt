package com.relic.scheduler

import android.content.Context
import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.relic.data.CommentRepository
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.domain.models.CommentModel
import com.relic.domain.models.PostModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import timber.log.Timber
import javax.inject.Inject

private const val KEY_POST_SOURCE = "KEY_POST_SOURCE"
private const val KEY_POST_PAGES_TO_SYNC = "KEY_POST_SOURCE"
private const val KEY_ENABLE_COMMENT_SYNC = "KEY_POST_SOURCE"

class PostSyncWorker(
    appContext: Context,
    private val workerParams: WorkerParameters,
    private val postRepo: PostRepository,
    private val commentRepo: CommentRepository
) : Worker(appContext, workerParams), CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { _, e ->
        Timber.e(e,  "caught exception")
    }

    class Factory @Inject constructor(
      private val postRepository : PostRepository,
      private val commentRepo: CommentRepository
    ) : WorkerFactory() {
        override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {
            Timber.d(postRepository.toString())
            return PostSyncWorker(appContext, workerParameters, postRepository, commentRepo)
        }
    }

    override fun doWork(): Result {
        Timber.d("do work ")
        // todo add support for other post sources
        val postSource = PostSource.Subreddit(workerParams.inputData.getString(KEY_POST_SOURCE)!!)
        val pagesToSync = workerParams.inputData.getInt(KEY_POST_PAGES_TO_SYNC, 0)
        val syncComments = workerParams.inputData.getBoolean(KEY_ENABLE_COMMENT_SYNC, false)

        return runBlocking { syncPosts(postSource, pagesToSync, syncComments) }
    }

    private suspend fun syncPosts(
      postSource: PostSource,
      pagesToSync: Int,
      syncComments: Boolean
    ): Result {
        val retrievedPosts = ArrayList<PostModel>()
        val retrievedComments = ArrayList<CommentModel>()
        var syncedPages = 0
        var next : String? = null

        val job = coroutineScope {
            async {
                while (syncedPages < pagesToSync) {
                    if (next == null && syncedPages == 0) {
                        break
                    }

                    val postsListing = postRepo.retrieveSortedPosts(postSource, SortType.DEFAULT, SortScope.NONE)
                    postsListing.data.children?.let { posts ->
                        retrievedPosts.addAll(posts)

                        // sync comments for each post
                        if (syncComments) {
                            posts.forEach { post ->
                                val commentAndPostData = commentRepo.retrieveComments(post.subreddit!!, post.fullName, true)
                                retrievedComments.addAll(commentAndPostData.comments)
                            }
                        }
                    }

                    next = postsListing.data.after
                    syncedPages += 1
                }
            }
        }

        // store all posts and comments
        postRepo.insertPosts(postSource, retrievedPosts)
        commentRepo.insertComments(retrievedComments)


        return try {
            job.await()
            Result.success()
        } catch (e : Exception) {
            Timber.e(e)
            Result.failure()
        }
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