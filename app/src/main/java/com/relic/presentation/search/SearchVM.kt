package com.relic.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.PostRepository
import com.relic.data.PostSource
import com.relic.data.SubRepository
import com.relic.data.UserRepository
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.main.RelicError
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

sealed class SearchResultType {
    object SUB : SearchResultType()
    object POST : SearchResultType()
    object USER : SearchResultType()
}

class SearchVM(
    private val subRepo: SubRepository,
    private val postRepo: PostRepository,
    private val userRepo: UserRepository
) : RelicViewModel(), DisplaySearchContract.SubredditSearchVM {

    class Factory @Inject constructor(
        private val subRepo: SubRepository,
        private val postRepo: PostRepository,
        private val userRepo: UserRepository
    ) {
        fun create(): SearchVM {
            return SearchVM(subRepo, postRepo, userRepo)
        }
    }

    private val _subSearchErrorLiveData = MutableLiveData<RelicError?>()
    private val _subredditResultsLiveData = MutableLiveData<List<SubPreviewModel>>()
    private val _postResultsLiveData = MutableLiveData<List<PostModel>>()
    private val _userResultsLiveData = MutableLiveData<List<UserModel>>()
    private val _localSubredditResultsLiveData = MutableLiveData<List<SubredditModel>>()
    private val _navigationLiveData = SingleLiveData<NavigationData>()
    private val _loadingLiveData = MutableLiveData<Boolean>()

    override val subSearchErrorLiveData: LiveData<RelicError?> = _subSearchErrorLiveData
    override val subredditResultsLiveData: LiveData<List<SubPreviewModel>> = _subredditResultsLiveData
    val postResultsLiveData: LiveData<List<PostModel>> = _postResultsLiveData
    val userResultsLiveData: LiveData<List<UserModel>> = _userResultsLiveData
    override val subscribedSubredditResultsLiveData: LiveData<List<SubredditModel>> = _localSubredditResultsLiveData
    override val navigationLiveData: LiveData<NavigationData> = _navigationLiveData
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private var query: String? = null
    var currentSearchType: SearchResultType = SearchResultType.SUB

    init {
        // init empty search results
        _subredditResultsLiveData.postValue(emptyList())
        _localSubredditResultsLiveData.postValue(emptyList())
    }

    /**
     * some search options don't have a query (ie. changing the result type) so we set serparate
     * the updating of the query string from the search function
     */
    override fun updateQuery(newQuery: String?) {
        query = newQuery
    }

    override fun search(newOptions: SubredditSearchOptions) {
        val localQuery = query
        if (localQuery == null || localQuery.isEmpty()) {
            _subredditResultsLiveData.postValue(emptyList())
            _localSubredditResultsLiveData.postValue(emptyList())
        } else {
            _loadingLiveData.postValue(true)
            launch {
                when (currentSearchType) {
                    SearchResultType.SUB -> {
                        val searchResults = subRepo.searchSubreddits(localQuery, displayNSFW = true, exact = false)
                        _subredditResultsLiveData.postValue(searchResults)
                    }
                    SearchResultType.POST -> {
                        val searchResults = postRepo.searchSubPosts("all", localQuery)
                        _postResultsLiveData.postValue(searchResults.data.children)
                    }
                    SearchResultType.USER -> {
                        val searchResults = userRepo.searchUsers(localQuery)
                        _userResultsLiveData.postValue(searchResults)
                    }
                }
                _loadingLiveData.postValue(false)
            }
        }
    }

    fun changeSearchResultType(resultType: SearchResultType, newOptions: SubredditSearchOptions) {
        currentSearchType = resultType
        search(newOptions)
    }

    // region SubredditSearchDelegate

    override fun preview(subreddit: String) {
        val sub = PostSource.Subreddit(subreddit)
        _navigationLiveData.postValue(NavigationData.PreviewPostSource(sub))
    }

    override fun visit(subreddit: String) {
        val sub = PostSource.Subreddit(subreddit)
        _navigationLiveData.postValue(NavigationData.ToPostSource(sub))
    }

    override fun subscribe(subscribe: Boolean, subreddit: String) {
        launch(Dispatchers.Main) {
            subRepo.getSubGateway().subscribe(subscribe, subreddit)
        }
    }

    // endregion SubredditSearchDelegate

    override fun handleException(context: CoroutineContext, e: Throwable) {
        Timber.e(e)
    }

}