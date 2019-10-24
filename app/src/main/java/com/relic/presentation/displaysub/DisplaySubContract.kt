package com.relic.presentation.displaysub

import androidx.lifecycle.LiveData
import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.domain.models.PostModel
import com.relic.presentation.main.RelicError
import com.relic.presentation.util.MediaType

interface DisplaySubContract {
    interface ViewModel {
        fun changeSortingMethod(sortType: SortType? = null, sortScope: SortScope? = null)

        /**
         * retrieves more posts from the network
         * @param resetPosts : indicates whether the old posts should be cleared
         */
        fun retrieveMorePosts(resetPosts: Boolean)
        fun updateSubStatus(subscribe: Boolean)
    }

    interface PostAdapterDelegate {
        val navigationLiveData : LiveData<NavigationData>

        fun visitPost(post: PostModel)
        fun voteOnPost(post: PostModel, vote : Int)
        fun savePost(post: PostModel)
        fun previewUser(post: PostModel)

        fun onLinkPressed(post: PostModel)
        fun onNewReplyPressed(post: PostModel)
    }

    interface PostViewDelegate {
        fun onPostPressed()
        fun onPostSavePressed()
        fun onPostUpvotePressed()
        fun onPostDownvotePressed()

        fun onPostReply()
        fun onPostLinkPressed()
        fun onUserPressed()
    }
}

// TODO extract out of this interface
sealed class NavigationData {
    data class ToPost (
        val postId : String,
        val subredditName : String,
        val commentId : String? = null
    ) : NavigationData ()

    data class ToPostSource (
        val source : PostSource
    ) : NavigationData ()

    data class PreviewPostSource (
            val source : PostSource
    ) : NavigationData ()

    // specifically for posts
    data class ToImage (
        val thumbnail : String
    ) : NavigationData ()

    data class ToExternal (
        val url : String
    ) : NavigationData ()

    data class ToUserPreview (
        val username : String
    ) : NavigationData ()

    data class ToUser (
            val username : String
    ) : NavigationData ()

    data class ToMedia(
      val mediaType: MediaType,
      val mediaUrl: String
    ) : NavigationData()

    data class ToReply(
      val parentFullname: String
    ) : NavigationData()
}

data class DisplaySubInfoData (
    var sortingMethod : SortType,
    var sortingScope : SortScope
)


object NoResults: RelicError()
