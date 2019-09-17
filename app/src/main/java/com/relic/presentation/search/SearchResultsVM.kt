package com.relic.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.NoResults
import com.relic.presentation.main.RelicError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SearchResultsVM(
    private val postRepo: PostRepository,
    private val postGateway: PostGateway,
    private val postSource: PostSource
) :
    RelicViewModel(),
    DisplaySearchContract.SearchVM,
    DisplaySearchContract.PostsSearchVM,
    DisplaySubContract.PostAdapterDelegate
{
    private var postSearchAfter : String? = null
    private var query : String? = null

    private val _subResultsLiveData = MutableLiveData<List<SubredditModel>>()
    private val _userResultsLiveData = MutableLiveData<List<UserModel>>()
    private val _postResultsLiveData = MutableLiveData<List<PostModel>>()
    private val _offlinePostResultsLiveData = MutableLiveData<List<PostModel>>()

    private val _postSearchErrorLiveData = MutableLiveData<RelicError?>()

    val subResultsLiveData : LiveData<List<SubredditModel>> = _subResultsLiveData
    val userResultsLiveData : LiveData<List<UserModel>> = _userResultsLiveData

    override val postResultsLiveData: LiveData<List<PostModel>> = _postResultsLiveData
    override val offlinePostResultsLiveData : LiveData<List<PostModel>> = _offlinePostResultsLiveData

    override val postSearchErrorLiveData: LiveData<RelicError?> =  _postSearchErrorLiveData

    class Factory @Inject constructor(
        private val postRepo: PostRepository,
        private val postGateway: PostGateway
    ) {
        fun create(postSource: PostSource) : SearchResultsVM {
            return SearchResultsVM(postRepo, postGateway, postSource)
        }
    }

    override fun updateQuery(query: String) {
        this.query = query
    }

    override fun search() {
        launch(Dispatchers.Main) {
            val listing = when(postSource) {
                is PostSource.Subreddit -> {
                    query?.let {
                        postRepo.searchSubPosts(postSource.subredditName, it, true)
                    }
                }
                else -> {
                    null
                }
            }

            listing?.data?.let { data ->
                postSearchAfter = data.after
                _postResultsLiveData.postValue(data.children)
            }

            // search offline posts
            when(postSource) {
                is PostSource.Subreddit -> {
                    query?.let {
                        val offlinePosts = postRepo.searchOfflinePosts(postSource.subredditName, it, true)
                        _offlinePostResultsLiveData.postValue(offlinePosts)
                    }
                }
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
                        val newPosts = (_postResultsLiveData.value ?: emptyList<PostModel>()) + children
                        _postResultsLiveData.postValue(newPosts)
                    }
                }

            } else {
                Timber.d("No more posts available for this query")
            }
        }
    }


    // region post adapter delegate

    override fun visitPost(postFullname: String, subreddit: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun voteOnPost(postFullname: String, voteValue: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun savePost(postFullname: String, save: Boolean) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLinkPressed(url: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun previewUser(username: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // endregion post adapter delegate
}