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
) : ViewModel(), DisplaySubsContract.VM, AuthenticationCallback,
        RetrieveNextListingCallback, SubInfoDialogContract.Delegate {

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
    private var initialized: Boolean = false
    private var refreshing: Boolean = false

    private val subscribedSubsMediator =  MediatorLiveData <List<SubredditModel>> ()
    private val _searchResults = MediatorLiveData <List<String>> ()
    private var searchResults: LiveData<List<String>> = _searchResults


    init {
        // initialize the viewmodel only if it hasn't already been initialized
        if (!initialized) {
            Log.d(TAG, "subreddit initialized")

            _searchResults.value = null
            subscribedSubsMediator.value = null

            initializeObservers()
            initialized = true
        }
    }

    /**
     * Initialize observers for livedata
     */
    private fun initializeObservers() {
        //subscribedSubsMediator.addSource(subRepo.getSubscribedSubs(), subscribedSubsMediator::setValue);
        subscribedSubsMediator.addSource <List<SubredditModel>> (subRepository!!.subscribedSubs) { subscribedSubs ->
            Log.d(TAG, " subs loaded $subscribedSubs")
            //      if (subscribedSubs.isEmpty()) {
            //        // refresh the token even if the vm has already been initialized
            //        subRepo.retrieveAllSubscribedSubs();
            //        //authenticator.refreshToken(this);
            //      } else {
            subscribedSubsMediator.setValue(subscribedSubs)
            //      }
        }

        //    // Observe changes to keys to request new data
        //    subscribedSubsMediator.addSource(listingRepo.getKey(), (@Nullable String nextVal) -> {
        //      // retrieve posts only if the "after" value is empty
        //      subRepo.retrieveAllSubscribedSubs(nextVal);
        //    });
    }

    override fun getAllSubscribedSubsLoaded(): LiveData<Boolean> {
        return subRepository.allSubscribedSubsLoaded
    }

    /**
     * Exposes subscribed subs to the ui for binding
     * @return the livedata list of subscribed subs
     */
    override fun getSubscribedList(): LiveData<List<SubredditModel>> {
        return subscribedSubsMediator
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

    override fun getSearchResults(): LiveData<List<String>> {
        return searchResults
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

    override fun onNextListing(nextVal: String) {
        //    // retrieve posts only if the "after" value is empty
        //    if (nextVal == null) {
        //      subRepo.retrieveAllSubscribedSubs();
        //    }
    }

    // Start of SubInfoDialogContract delegate methods
    override fun updateSubscriptionStatus(newStatus: Boolean) {
    }

    override fun updatePinnedStatus(newStatus: Boolean) {
    }
    // End of SubInfoDialogContract delegate methods

}
