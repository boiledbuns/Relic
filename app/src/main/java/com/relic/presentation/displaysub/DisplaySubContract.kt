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

        fun visitPost(postFullname: String, subreddit : String)
        fun voteOnPost(postFullname: String, voteValue: Int)
        fun savePost(postFullname: String, save: Boolean)
        fun previewUser(username : String)

        fun onLinkPressed(postModel: PostModel)
        fun onNewReplyPressed(postFullname: String)
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
