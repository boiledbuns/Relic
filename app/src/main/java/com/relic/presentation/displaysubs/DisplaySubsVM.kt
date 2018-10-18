package com.relic.presentation.displaysubs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

import com.relic.data.Authenticator
import com.relic.data.ListingRepository
import com.relic.data.SubRepository
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.data.models.SubredditModel
import com.relic.presentation.callbacks.RetrieveNextListingCallback
import com.relic.presentation.subinfodialog.SubInfoDialogContract
import javax.inject.Inject

class DisplaySubsVM (
        private val subRepository: SubRepository,
        private val listingRepository: ListingRepository,
        private val authenticator: Authenticator
) : ViewModel(), DisplaySubsContract.VM, AuthenticationCallback, SubInfoDialogContract.Delegate {

    class Factory @Inject constructor(
            private val subRepository: SubRepository,
            private val listingRepository: ListingRepository,
            private val authenticator: Authenticator
    ) {
        fun create() : DisplaySubsVM{
            return DisplaySubsVM(subRepository, listingRepository, authenticator)
        }
    }

    private val TAG = "DISPLAY_SUBS_VM"
    private var refreshing: Boolean = false

    private val _subscribedSubsList =  MediatorLiveData <List<SubredditModel>> ()
    val subscribedSubsList : LiveData<List<SubredditModel>> =  _subscribedSubsList

    private val _searchResults = MediatorLiveData <List<String>> ()
    val searchResults: LiveData<List<String>> = _searchResults

    val pinnedSubs : LiveData<List<SubredditModel>> = subRepository.pinnedsubs
    val allSubscribedSubsLoaded : LiveData<Boolean> = subRepository.allSubscribedSubsLoaded

    init {
        _searchResults.value = null
        _subscribedSubsList.value = null

        initializeObservers()
    }

    /**
     * Initialize observers for livedata
     */
    private fun initializeObservers() {
        //subscribedSubsList.addSource(subRepo.getSubscribedSubs(), subscribedSubsList::setValue);
        _subscribedSubsList.addSource <List<SubredditModel>> (subRepository.subscribedSubs) { subscribedSubs ->
            Log.d(TAG, " subs loaded $subscribedSubs")
            //      if (subscribedSubs.isEmpty()) {
            //        // refresh the token even if the vm has already been initialized
            //        subRepo.retrieveAllSubscribedSubs();
            //        //authenticator.refreshToken(this);
            //      } else {
            _subscribedSubsList.setValue(subscribedSubs)
            //      }
        }

        //    // Observe changes to keys to request new data
        //    subscribedSubsList.addSource(listingRepo.getKey(), (@Nullable String nextVal) -> {
        //      // retrieve posts only if the "after" value is empty
        //      subRepo.retrieveAllSubscribedSubs(nextVal);
        //    });
    }

    /**
     * Retrieves more posts either from the start or leading off the current "after" page
     * @param resetPosts whether the post list should be restarted
     */
    override fun retrieveMoreSubs(resetPosts: Boolean) {
        if (resetPosts) {
            refreshing = true
            // refresh token before performing any requests
            authenticator.refreshToken(this)
        }
    }

    override fun retrieveSearchResults(query: String) {
        Log.d(TAG, "Retrieving search results for $query")

        // retrieves search results only if the query is non empty
        // also ignores first query entry (always empty)
        if (!query.isEmpty()) {
            // replaces the current livedata with a new one based on new query string
            subRepository.searchSubreddits(_searchResults, query)
        }
    }

    override fun onAuthenticated() {
        //listingRepo.retrieveKey("SUB_REPO");
        Log.d(TAG, "On authenticated called")
        subRepository.retrieveAllSubscribedSubs()
    }

    // Start of SubInfoDialogContract delegate methods
    override fun updateSubscriptionStatus(newStatus: Boolean) {
    }

    override fun updatePinnedStatus(newStatus: Boolean) {
    }
    // End of SubInfoDialogContract delegate methods

}
