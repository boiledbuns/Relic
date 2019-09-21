package com.relic.presentation.search

import androidx.lifecycle.LiveData
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.main.RelicError

interface DisplaySearchContract {

    interface PostSearchVM : DisplaySubContract.PostAdapterDelegate {
        val postSearchErrorLiveData : LiveData<RelicError?>
        val postResultsLiveData : LiveData<List<PostModel>>
        val offlinePostResultsLiveData : LiveData<List<PostModel>>

        fun updateQuery(query : String)
        fun search(options : PostSearchOptions)
        fun retrieveMorePostResults()
    }

    interface SubredditSearchVM {
        val subSearchErrorLiveData : LiveData<RelicError?>
        val subredditResultsLiveData : LiveData<List<SubPreviewModel>>
        val subscribedSubredditResultsLiveData : LiveData<List<SubredditModel>>

        fun updateQuery(newQuery : String)
        fun search(newOptions : SubredditSearchOptions)
        fun retrieveMoreSubResults()
    }

    interface UserSearchVM {
        val errorLiveData : LiveData<RelicError?>
        val searchResults : LiveData<List<UserModel>>

        fun updateQuery(newQuery : String)
        fun search(newOptions : UserSearchOptions)
        fun retrieveMoreUsers()
    }
}

sealed class SearchSource {
    data class Subreddit(
        val name : String
    )
}

sealed class SubredditSearchResult {
    data class Name(
        val name: String
    ) : SubredditSearchResult()

    data class Full(
        val subreddit: SubredditModel
    ) : SubredditSearchResult()
}

data class UserSearchOptions(
    val restrictToSource : Boolean = false
)

data class SubredditSearchOptions(
    val restrictToSource : Boolean = false
)

data class PostSearchOptions(
        val restrictToSource : Boolean = false
)

data class CommentSearchOptions(
    val restrictToSource : Boolean = false
)