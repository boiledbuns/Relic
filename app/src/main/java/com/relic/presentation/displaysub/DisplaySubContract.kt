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
}

sealed class NavigationData {
    data class ToPost (
            val postId : String,
            val subredditName : String,
            val postSource: PostRepository.PostSource
    ) : NavigationData ()

    data class ToImage (
            val thumbnail : String
    ) : NavigationData ()

    data class ToExternal (
        val url : String
    ) : NavigationData ()
}

data class DisplaySubInfoData (
    var sortingMethod : PostRepository.SortType,
    var sortingScope : PostRepository.SortScope
)