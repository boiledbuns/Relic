package com.relic.presentation.displaysub

import com.relic.data.PostRepository

interface DisplaySubContract {
    interface ViewModel {
        fun changeSortingMethod(sortType: PostRepository.SortType? = null, sortScope: PostRepository.SortScope? = null)
        fun retrieveMorePosts(resetPosts: Boolean)
        fun updateSubStatus(subscribe: Boolean)
    }

    interface PostAdapterDelegate {
        fun visitPost(postFullname: String, subreddit : String)
        fun voteOnPost(postFullname: String, voteValue: Int)
        fun savePost(postFullname: String, save: Boolean)
        fun onThumbnailClicked(postThumbnailUrl: String)
    }

    interface PostItemAdapterDelegate {
        fun onPostPressed(itemPosition : Int)
        fun onPostSavePressed(itemPosition : Int)
        fun onPostUpvotePressed(itemPosition : Int)
        fun onPostDownvotePressed(itemPosition : Int)
        fun onPostLinkPressed(itemPosition : Int)
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
}

data class DisplaySubInfoData (
    var sortingMethod : PostRepository.SortType,
    var sortingScope : PostRepository.SortScope
)

sealed class SubExceptionData : Exception() {
    object NoPosts : SubExceptionData()
    object NetworkUnavailable : SubExceptionData()
    object UnexpectedException : SubExceptionData()
}