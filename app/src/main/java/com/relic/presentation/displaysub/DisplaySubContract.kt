package com.relic.presentation.displaysub

import android.arch.lifecycle.LiveData
import com.relic.data.PostRepository
import com.relic.data.models.PostModel

interface DisplaySubContract {
    interface ViewModel {
        fun changeSortingMethod(sortType: PostRepository.SortType? = null, sortScope: PostRepository.SortScope? = null)
        fun retrieveMorePosts(resetPosts: Boolean)
        fun updateSubStatus(subscribe: Boolean)
    }

    interface SearchVM {
        val searchResults : LiveData<List<PostModel>>
        fun search(query : String)
    }

    interface PostAdapterDelegate {
        fun visitPost(postFullname: String, subreddit : String)
        fun voteOnPost(postFullname: String, voteValue: Int)
        fun savePost(postFullname: String, save: Boolean)
        fun onLinkPressed(url: String)
        fun previewUser(username : String)
    }

    interface PostItemAdapterDelegate {
        fun onPostPressed(itemPosition : Int)
        fun onPostSavePressed(itemPosition : Int)
        fun onPostUpvotePressed(itemPosition : Int, notify : Boolean = true)
        fun onPostDownvotePressed(itemPosition : Int, notify : Boolean = true)
        fun onPostLinkPressed(itemPosition : Int)
        fun onUserPressed(itemPosition : Int)
    }
}

sealed class SubNavigationData {
    data class ToPost (
        val postId : String,
        val subredditName : String,
        val postSource: PostRepository.PostSource,
        val commentId : String? = null
    ) : SubNavigationData ()

    data class ToImage (
        val thumbnail : String
    ) : SubNavigationData ()

    data class ToExternal (
        val url : String
    ) : SubNavigationData ()

    data class ToUserPreview (
        val username : String
    ) : SubNavigationData ()
}

data class DisplaySubInfoData (
    var sortingMethod : PostRepository.SortType,
    var sortingScope : PostRepository.SortScope
)

/**
 * note: this isn't an exception; it's represents data to be displayed to user in case of failure
 */
sealed class SubError {
    object NoPosts : SubError()
    object NetworkUnavailable : SubError()
    object UnexpectedException : SubError()
}