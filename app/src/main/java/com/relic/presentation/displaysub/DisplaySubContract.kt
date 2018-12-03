package com.relic.presentation.displaysub

interface DisplaySubContract {
    interface ViewModel {
        fun changeSortingMethod(sortingCode: Int? = null, sortScope: Int? = null)

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
            val subredditName : String
    ) : NavigationData ()

    data class ToImage (
            val thumbnail : String
    ) : NavigationData ()

    data class ToExternal (
        val url : String
    ) : NavigationData ()
}

data class DisplaySubInfoData (
    var sortingMethod : Int,
    var sortingScope : Int
)