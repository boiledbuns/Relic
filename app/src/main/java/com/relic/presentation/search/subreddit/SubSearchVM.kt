package com.relic.presentation.search.subreddit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.data.PostSource
import com.relic.data.SubRepository
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.main.RelicError
import com.relic.presentation.search.DisplaySearchContract
import com.relic.presentation.search.SubredditSearchOptions
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class SubSearchVM(
    private val subRepo: SubRepository
) : RelicViewModel(), DisplaySearchContract.SubredditSearchVM {

    class Factory @Inject constructor(
        private val subRepo: SubRepository
    ) {
        fun create(): SubSearchVM {
            return SubSearchVM(subRepo)
        }
    }

    private val _subSearchErrorLiveData = MutableLiveData<RelicError?>()
    private val _subredditResultsLiveData = MutableLiveData<List<SubPreviewModel>>()
    private val _subscribedSubredditResultsLiveData = MutableLiveData<List<SubredditModel>>()
    private val _navigationLiveData = SingleLiveData<NavigationData>()

    override val subSearchErrorLiveData: LiveData<RelicError?> = _subSearchErrorLiveData
    override val subredditResultsLiveData: LiveData<List<SubPreviewModel>> = _subredditResultsLiveData
    override val subscribedSubredditResultsLiveData: LiveData<List<SubredditModel>> = _subscribedSubredditResultsLiveData
    override val navigationLiveData : LiveData<NavigationData> = _navigationLiveData

    private var query : String? = null

    init {
        // init empty search results
        _subredditResultsLiveData.postValue(emptyList())
        _subscribedSubredditResultsLiveData.postValue(emptyList())
    }

    override fun updateQuery(newQuery: String) {
        query = newQuery
    }

    override fun search(newOptions: SubredditSearchOptions) {
        if (query.isNullOrEmpty()) {
            _subredditResultsLiveData.postValue(emptyList())
        } else {
            query?.let {
                launch {
                    val onlineResults = subRepo.searchSubreddits(it, displayNSFW = true, exact = false)
                    _subredditResultsLiveData.postValue(onlineResults)

                    val offlineResults = subRepo.searchOfflineSubreddits(it)
                    _subscribedSubredditResultsLiveData.postValue(offlineResults)
                }
            }
        }
    }

    // region SubredditSearchDelegate

    override fun preview(subreddit : String) {
        val sub = PostSource.Subreddit(subreddit)
        _navigationLiveData.postValue(NavigationData.PreviewPostSource(sub))
    }

    override fun visit(subreddit : String) {
        val sub = PostSource.Subreddit(subreddit)
        _navigationLiveData.postValue(NavigationData.ToPostSource(sub))
    }

    override fun subscribe(subscribe : Boolean, subreddit : String) {
        launch(Dispatchers.Main) {
            subRepo.getSubGateway().subscribe(subscribe, subreddit)
        }
    }

    // endregion SubredditSearchDelegate

    override fun retrieveMoreSubResults() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun handleException(context: CoroutineContext, e: Throwable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}