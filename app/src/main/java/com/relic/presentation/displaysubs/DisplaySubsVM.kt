package com.relic.presentation.displaysubs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.util.Log

import com.relic.data.auth.AuthImpl
import com.relic.data.ListingRepository
import com.relic.data.SubRepository
import com.relic.data.SubsLoadedCallback
import com.relic.data.models.SubredditModel
import com.relic.presentation.base.RelicViewModel
import com.relic.presentation.subinfodialog.SubInfoDialogContract
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DisplaySubsVM (
        private val subRepository: SubRepository,
        private val listingRepository: ListingRepository,
        private val authenticator: AuthImpl
) : RelicViewModel(), DisplaySubsContract.VM, SubInfoDialogContract.Delegate {

    class Factory @Inject constructor(
            private val subRepository: SubRepository,
            private val listingRepository: ListingRepository,
            private val authenticator: AuthImpl
    ) {
        fun create() : DisplaySubsVM{
            return DisplaySubsVM(subRepository, listingRepository, authenticator)
        }
    }

    private var refreshing: Boolean = false

    private val _subscribedSubsList = MediatorLiveData <List<SubredditModel>> ()
    val subscribedSubsList : LiveData<List<SubredditModel>> =  _subscribedSubsList

    private val _searchResults = MediatorLiveData <List<String>> ()
    val searchResults: LiveData<List<String>> = _searchResults

    val pinnedSubs : LiveData<List<SubredditModel>> = subRepository.getPinnedsubs()

    init {
        initializeObservers()
    }

    /**
     * Initialize observers for livedata
     */
    private fun initializeObservers() {
        //subscribedSubsList.addSource(subRepo.getSubscribedSubs(), subscribedSubsList::setValue);
        _subscribedSubsList.addSource <List<SubredditModel>> (subRepository.getSubscribedSubs()) { subscribedSubs ->

            //      if (subscribedSubs.isEmpty()) {
            //        // refresh the token even if the vm has already been initialized
            //        subRepo.retrieveAllSubscribedSubs();
            //        //authenticator.refreshToken(this);
            //      } else {

            if (!refreshing) {
                Log.d(TAG, "subs loaded $subscribedSubs")
                _subscribedSubsList.postValue(subscribedSubs)
            }
            //      }
        }
    }

    /**
     * Retrieves more posts either from the start or leading off the current "after" page
     * @param resetPosts whether the post list should be restarted
     */
    override fun retrieveMoreSubs(resetPosts: Boolean) {
        if (resetPosts) {
            refreshing = true
            launch(Dispatchers.Main) {
                subRepository.retrieveAllSubscribedSubs(
                    SubsLoadedCallback { refreshing = false }
                )
            }
        }
    }

    override fun retrieveSearchResults(query: String) {
        Log.d(TAG, "Retrieving search results for $query")

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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
