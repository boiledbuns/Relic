package com.relic.presentation.displaysub

interface DisplaySubContract {
    interface ViewModel {
        fun changeSortingMethod(sortingCode: Int, sortScope: Int)

        fun retrieveMorePosts(resetPosts: Boolean)

        fun updateSubStatus(subscribe: Boolean)
    }

    interface PostAdapterDelegate {
        fun visitPost(postFullname: String)
        fun voteOnPost(postFullname: String, voteValue: Int)
        fun savePost(postFullname: String, save: Boolean)
        fun showImage(postThumbnailUrl: String)
    }
}

sealed class NavigationData {
    data class ToPost (
            val postId : String,
            val subredditName : String
    ) : NavigationData ()

    data class ToImage (
            val thumbnail : String
    ) : NavigationData ()
}