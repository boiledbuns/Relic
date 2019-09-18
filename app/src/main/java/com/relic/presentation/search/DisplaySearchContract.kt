package com.relic.presentation.search

import androidx.lifecycle.LiveData
import com.relic.domain.models.PostModel
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.main.RelicError

interface DisplaySearchContract {

    interface SearchVM {
        fun updateQuery(query : String)
        fun search(options : SearchOptions)
    }

    interface PostsSearchVM : DisplaySubContract.PostAdapterDelegate {
        val postSearchErrorLiveData : LiveData<RelicError?>
        val postResultsLiveData : LiveData<List<PostModel>>
        val offlinePostResultsLiveData : LiveData<List<PostModel>>

        fun search(options : SearchOptions)
        fun retrieveMorePostResults()
    }
}

sealed class SearchSource {
    data class Subreddit(
        val name : String
    )
}

data class SearchOptions(
    val restrictToSource : Boolean = false
)