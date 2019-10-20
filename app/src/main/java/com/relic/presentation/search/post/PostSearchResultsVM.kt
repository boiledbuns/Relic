package com.relic.presentation.search.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.PostModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.NoResults
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.DisplaySearchContract
import com.relic.presentation.search.PostSearchOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class PostSearchResultsVM(
    private val postRepo: PostRepository,
    private val postSource: PostSource
) : RelicViewModel(), DisplaySearchContract.PostSearchVM {
    class Factory @Inject constructor(
        private val postRepo: PostRepository
    ) {
        fun create(postSource: PostSource) : PostSearchResultsVM {
            return PostSearchResultsVM(postRepo, postSource)
        }
    }

    private var postSearchAfter : String? = null
    private var query : String? = null

    private val _postResultsLiveData = MutableLiveData<List<PostModel>>()
    private val _offlinePostResultsLiveData = MutableLiveData<List<PostModel>>()

    private val _postSearchErrorLiveData = MutableLiveData<RelicError?>()

    override val postResultsLiveData: LiveData<List<PostModel>> = _postResultsLiveData
    override val offlinePostResultsLiveData : LiveData<List<PostModel>> = _offlinePostResultsLiveData

    override val postSearchErrorLiveData: LiveData<RelicError?> =  _postSearchErrorLiveData

    override fun updateQuery(query: String) {
        this.query = query
    }

    override fun search(options : PostSearchOptions) {
        query?.let {
            launch(Dispatchers.Main) {
                val listing = when (postSource) {
                    is PostSource.Subreddit -> {
                        postRepo.searchSubPosts(postSource.subredditName, it, true)
                    }
                    else -> null
                }

                listing?.data?.let { data ->
                    postSearchAfter = data.after
                    _postResultsLiveData.postValue(data.children)
                }

                // search offline posts
                val offlinePosts = postRepo.searchOfflinePosts(postSource, it)
                _offlinePostResultsLiveData.postValue(offlinePosts)
            }
        }
    }

    override fun retrieveMorePostResults() {
        val currQuery = query
        launch(Dispatchers.Main) {
            if (postSearchAfter != null && currQuery != null){
                val listing = when (postSource) {
                    is PostSource.Subreddit -> {
                        postRepo.searchSubPosts(postSource.subredditName, currQuery, true, postSearchAfter)
                    }
                    else -> {
                        null
                    }
                }

                listing?.data?.let { data ->
                    postSearchAfter = data.after
                    val children = data.children
                    // show appropriate message to user to indicate no more posts could be found
                    if (children.isNullOrEmpty()) {
                        _postSearchErrorLiveData.postValue(NoResults)
                    } else {
                        // concat new results with current ones
                        val newPosts = (_postResultsLiveData.value ?: emptyList()) + children
                        _postResultsLiveData.postValue(newPosts)
                    }
                }

            } else {
                Timber.d("No more posts available for this query")
                _postSearchErrorLiveData.postValue(NoResults)
            }
        }
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        Timber.e(e)
    }
}