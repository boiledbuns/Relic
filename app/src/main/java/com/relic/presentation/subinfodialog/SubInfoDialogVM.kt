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
    private val subredditLiveData : LiveData<SubredditModel> = _subredditLiveData

    init {
        //
    }

    fun updateSubscriptionStatus(subscribed: Boolean) {
        // check if subreddit is currentylu pinned
    }

    fun pinSubreddit(pinned : Boolean) {
        subRepo.pinSubreddit(subredditName, true)
    }
}