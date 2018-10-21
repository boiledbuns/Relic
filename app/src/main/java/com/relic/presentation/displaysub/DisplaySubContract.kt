package com.relic.presentation.displaysub

import android.arch.lifecycle.LiveData
import android.view.View

import com.relic.data.PostRepository
import com.relic.data.SubRepository
import com.relic.data.models.PostModel
import com.relic.data.models.SubredditModel

interface DisplaySubContract {
    interface ViewModel {

        /**
         * Used to check if the viewmodel has already been initialized
         * @return
         */
        val isInitialized: Boolean

        val subModel: LiveData<SubredditModel>

        val posts: LiveData<List<PostModel>>

        // LiveData<Boolean> getIsSubscribed();

        val subName: String
        fun init(subredditName: String, subRepo: SubRepository, postRepo: PostRepository)

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