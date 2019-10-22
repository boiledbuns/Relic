package com.relic.presentation.displaysubs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.relic.domain.models.SubredditModel
import com.relic.presentation.displaysub.NavigationData

interface DisplaySubsContract {
    interface VM {
        fun refreshSubs()
        /**
         * Makes request for matches based on the query
         * @param query search query to be sent to api
         */
        fun retrieveSearchResults(query: String)
    }

    interface SubAdapterDelegate {
        fun onClick(subItem: SubredditModel)
        fun onLongClick(subItem: SubredditModel)
        fun onSubscribe(subscribe : Boolean, subreddit: String)

        val navigationLiveData: LiveData<NavigationData>
    }
}