package com.relic.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.PostRepository
import com.relic.data.SubRepository
import com.relic.data.UserRepository
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.main.RelicError
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
    private val _loadingLiveData = MutableLiveData<Boolean>()

    override val subSearchErrorLiveData: LiveData<RelicError?> = _subSearchErrorLiveData
    override val subredditResultsLiveData: LiveData<List<SubPreviewModel>> = _subredditResultsLiveData
    val postResultsLiveData: LiveData<List<PostModel>> = _postResultsLiveData
    val userResultsLiveData: LiveData<List<UserModel>> = _userResultsLiveData
    override val subscribedSubredditResultsLiveData: LiveData<List<SubredditModel>> = _localSubredditResultsLiveData
    val loadingLiveData: LiveData<Boolean> = _loadingLiveData

    private var localQuery: String? = null
    var currentSearchType: SearchResultType = SearchResultType.SUB

    init {
        // init empty search results
        _subredditResultsLiveData.postValue(emptyList())
        _localSubredditResultsLiveData.postValue(emptyList())
    }


    override fun search(query: String?, newOptions: SubredditSearchOptions) {
        if (localQuery == query) return
        localQuery = query
        search(newOptions)
    }

    fun changeSearchResultType(resultType: SearchResultType, newOptions: SubredditSearchOptions) {
        currentSearchType = resultType
        search(newOptions)
    }

    private fun search(newOptions: SubredditSearchOptions) {
        val searchQuery = localQuery
        if (searchQuery == null || searchQuery.isEmpty()) {
            _subredditResultsLiveData.postValue(emptyList())
            _localSubredditResultsLiveData.postValue(emptyList())
        } else {
            _loadingLiveData.postValue(true)
            launch {
                when (currentSearchType) {
                    SearchResultType.SUB -> {
                        val searchResults = subRepo.searchSubreddits(searchQuery, displayNSFW = true, exact = false)
                        _subredditResultsLiveData.postValue(searchResults)
                    }
                    SearchResultType.POST -> {
                        val searchResults = postRepo.searchSubPosts("all", searchQuery)
                        _postResultsLiveData.postValue(searchResults.data.children)
                    }
                    SearchResultType.USER -> {
                        val searchResults = userRepo.searchUsers(searchQuery)
                        _userResultsLiveData.postValue(searchResults.data.children)
                    }
                }
                _loadingLiveData.postValue(false)
            }
        }
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        Timber.e(e)
    }
}