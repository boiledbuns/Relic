package com.relic.presentation.subinfodialog

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.relic.data.SubRepository
import com.relic.data.models.SubredditModel
import javax.inject.Inject

class SubInfoDialogVM (
        private val subRepo: SubRepository,
        private val subredditName: String
) : ViewModel () {

    class Factory @Inject constructor(private val subRepository: SubRepository) {
        fun create(subredditName : String) : SubInfoDialogVM{
            return SubInfoDialogVM(subRepository, subredditName)
        }
    }

    private val _subredditLiveData = subRepo.getSingleSub(subredditName)
    val subredditLiveData : LiveData<SubredditModel> = _subredditLiveData

    private val _sideBarLiveData = subRepo.subGateway.getSidebar(subredditName)
    val sideBarLiveData : LiveData<String> = _sideBarLiveData

    init {
        subRepo.subGateway.getAdditionalSubInfo(subredditName)
    }

    fun updateSubscriptionStatus(subscribed: Boolean) {
        // check if subreddit is currently pinned
    }

    fun pinSubreddit(pinned : Boolean) {
        subRepo.pinSubreddit(subredditName, true)
    }
}