package com.relic.presentation.displaysubs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.relic.data.Auth

import com.relic.data.ListingRepository
import com.relic.data.SubRepository
import com.relic.domain.models.SubredditModel
import com.relic.network.NetworkUtil
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.subinfodialog.SubInfoDialogContract
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DisplaySubsVM (
    private val subRepository: SubRepository,
    private val listingRepository: ListingRepository,
    private val networkUtil: NetworkUtil,
    private val authenticator: Auth
) : RelicViewModel(), DisplaySubsContract.VM, SubInfoDialogContract.Delegate {

    class Factory @Inject constructor(
            private val subRepository: SubRepository,
            private val listingRepository: ListingRepository,
            private val networkUtil: NetworkUtil,
            private val authenticator: Auth
    ) {
        fun create() : DisplaySubsVM{
            return DisplaySubsVM(subRepository, listingRepository, networkUtil, authenticator)
        }
    }

    private val _subscribedSubsList = MediatorLiveData <List<SubredditModel>> ()
    val subscribedSubsList : LiveData<List<SubredditModel>> =  _subscribedSubsList

    private val _searchResults = MediatorLiveData <List<String>> ()
    val searchResults: LiveData<List<String>> = _searchResults

    val pinnedSubs : LiveData<List<SubredditModel>> = subRepository.getPinnedsubs()

    init {
        _subscribedSubsList.addSource(subRepository.getSubscribedSubs()) { subscribedSubs ->
            _subscribedSubsList.postValue(subscribedSubs)
        }

        refreshSubs()
    }

    override fun refreshSubs() {
        launch (Dispatchers.Main) {
            val posts = subRepository.retrieveAllSubscribedSubs()
            subRepository.clearAndInsertSubs(posts)
        }
    }

    override fun retrieveSearchResults(query: String) {
        Timber.d("Retrieving search results for $query")

        // retrieves search results only if the query is non empty
        // also ignores first query entry (always empty)
        if (!query.isEmpty()) {
            // replaces the current livedata with a new one based on new query string
            launch(Dispatchers.Main) { subRepository.searchSubreddits(query) }
        }
    }

    // Start of SubInfoDialogContract delegate methods
    override fun updateSubscriptionStatus(newStatus: Boolean) {
    }

    override fun updatePinnedStatus(newStatus: Boolean) {
    }
    // End of SubInfoDialogContract delegate methods

    override fun handleException(context: CoroutineContext, e: Throwable) {
        // TODO decide whether to display or not
    }
}
