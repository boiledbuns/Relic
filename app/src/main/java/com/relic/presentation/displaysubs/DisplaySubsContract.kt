package com.relic.presentation.displaysubs

import androidx.lifecycle.LiveData
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
        fun interact(subreddit: SubredditModel, subInteraction: SubInteraction)

        val navigationLiveData: LiveData<NavigationData>
    }

    sealed class SubInteraction {
        object Visit : SubInteraction()
        object Preview : SubInteraction()
        object Subscribe : SubInteraction()
    }
}