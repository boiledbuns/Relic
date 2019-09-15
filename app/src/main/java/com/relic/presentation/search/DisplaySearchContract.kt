package com.relic.presentation.search

import android.arch.lifecycle.LiveData
import com.relic.domain.models.PostModel
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.main.RelicError

interface DisplaySearchContract {

    interface SearchVM {
        fun updateQuery(query : String)
        fun search()
    }

    interface PostsSearchVM : DisplaySubContract.PostAdapterDelegate {
        val postSearchErrorLiveData : LiveData<RelicError>
        val postResultsLiveData : LiveData<List<PostModel>>

        fun search()
        fun retrieveMorePostResults()
    }
}

sealed class searchSource {
    data class Subreddit(
        val name : String
    )
}