package com.relic.presentation.displaysubs

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MediatorLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log

import com.relic.data.auth.AuthenticatorImpl
import com.relic.data.ListingRepository
import com.relic.data.SubRepository
import com.relic.presentation.callbacks.AuthenticationCallback
import com.relic.data.models.SubredditModel
import com.relic.presentation.subinfodialog.SubInfoDialogContract
import javax.inject.Inject

class DisplaySubsVM (
        private val subRepository: SubRepository,
        private val listingRepository: ListingRepository,
        private val authenticator: AuthenticatorImpl
) : ViewModel(), DisplaySubsContract.VM, AuthenticationCallback, SubInfoDialogContract.Delegate {

    class Factory @Inject constructor(
            private val subRepository: SubRepository,
            private val listingRepository: ListingRepository,
            private val authenticator: AuthenticatorImpl
    ) {
        fun create() : DisplaySubsVM{
            return DisplaySubsVM(subRepository, listingRepository, authenticator)
        }
    }

    private val TAG = "DISPLAY_SUBS_VM"
    private var refreshing: Boolean = false

    private val _subscribedSubsList = MediatorLiveData <List<SubredditModel>> ()
    val subscribedSubsList : LiveData<List<SubredditModel>> =  _subscribedSubsList

    private val _searchResults = MediatorLiveData <List<String>> ()
    val searchResults: LiveData<List<String>> = _searchResults

    val pinnedSubs : LiveData<List<SubredditModel>> = subRepository.pinnedsubs

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
        Log.d(TAG, "On authenticated called")

        subRepository.retrieveAllSubscribedSubs {
            refreshing = false
        }
    }

    // Start of SubInfoDialogContract delegate methods
    override fun updateSubscriptionStatus(newStatus: Boolean) {
    }

    override fun updatePinnedStatus(newStatus: Boolean) {
    }
    // End of SubInfoDialogContract delegate methods

}
