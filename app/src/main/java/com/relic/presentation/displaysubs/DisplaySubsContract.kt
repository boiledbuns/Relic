package com.relic.presentation.displaysubs

interface DisplaySubsContract {
    interface VM {
        fun refreshSubs()
        /**
         * Makes request for matches based on the query
         * @param query search query to be sent to api
         */
        fun retrieveSearchResults(query: String)
    }

}