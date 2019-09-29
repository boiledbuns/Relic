package com.relic.presentation.search

import androidx.lifecycle.LiveData
import com.relic.domain.models.PostModel
import com.relic.domain.models.SubPreviewModel
import com.relic.domain.models.SubredditModel
import com.relic.domain.models.UserModel
import com.relic.presentation.displaysub.DisplaySubContract
import com.relic.presentation.displaysub.NavigationData
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

    interface SubredditSearchVM : SubredditSearchDelegate {
        val subSearchErrorLiveData : LiveData<RelicError?>
        val subredditResultsLiveData : LiveData<List<SubPreviewModel>>
        val subscribedSubredditResultsLiveData : LiveData<List<SubredditModel>>
        val navigationLiveData : LiveData<NavigationData>

        fun updateQuery(newQuery : String)
        fun search(newOptions : SubredditSearchOptions)
    }

    interface UserSearchVM {
        val errorLiveData : LiveData<RelicError?>
        val searchResultsLiveData : LiveData<UserSearchResults>
        val navigationLiveData : LiveData<NavigationData>

        fun updateQuery(newQuery : String)
        fun search(newOptions : UserSearchOptions)

        /**
         * opens profile for user with username as supplied query
         * if username is not supplied -> uses the result of the most recent query
         */
        fun openUser(username : String? = null)
    }
}

interface SubredditSearchDelegate {
    fun visit(subreddit : String)
    fun preview(subreddit : String)
    fun subscribe(subscribe : Boolean, subreddit : String)
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

data class UserSearchResults(
    val query : String,
    var user : UserModel?
)