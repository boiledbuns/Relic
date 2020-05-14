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

    val subscribedSubsList : LiveData<List<SubredditModel>>
        get() = subRepository.getSubscribedSubs()
//    val subscribedSubsList by lazy { subRepository.getSubscribedSubs() }

    private val _searchResults = MediatorLiveData <List<String>> ()
    val searchResults: LiveData<List<String>> = _searchResults

    val pinnedSubs : LiveData<List<SubredditModel>> = subRepository.getPinnedsubs()

    private var searchDisplayNSFW : Boolean = false
    private var searchExact : Boolean = false

    init {
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
            launch(Dispatchers.Main) { subRepository.searchSubreddits(query, searchDisplayNSFW, searchExact) }
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
