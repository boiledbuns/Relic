package com.relic.presentation.displaysub

import androidx.lifecycle.LiveData
import com.relic.data.PostSource
import com.relic.data.SortScope
import com.relic.data.SortType
import com.relic.domain.models.PostModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.UPVOTE_PRESSED
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

    // delegate for handling domain layer interactions
    interface PostAdapterDelegate {
        val navigationLiveData : LiveData<NavigationData>
        fun handlePostInteraction(interaction: PostInteraction)
    }


}

abstract class PostViewDelegate(
    private val postInteractor: DisplaySubContract.PostAdapterDelegate
){
    fun onPostPressed() = postInteractor.handlePostInteraction(PostInteraction.Visit(getPost()))
    fun onPostSavePressed() = postInteractor.handlePostInteraction(PostInteraction.Save(getPost()))
    fun onPostUpvotePressed() = postInteractor.handlePostInteraction(PostInteraction.Vote(getPost(), UPVOTE_PRESSED))
    fun onPostDownvotePressed() = postInteractor.handlePostInteraction(PostInteraction.Vote(getPost(), DOWNVOTE_PRESSED))
    fun onPostReply() = postInteractor.handlePostInteraction(PostInteraction.NewReply(getPost()))
    fun onPostLinkPressed() = postInteractor.handlePostInteraction(PostInteraction.VisitLink(getPost()))
    fun onUserPressed() = postInteractor.handlePostInteraction(PostInteraction.PreviewUser(getPost()))

    abstract fun getPost() : PostModel
}

sealed class PostInteraction(
    open val post : PostModel
) {
    data class Visit(override val post: PostModel) : PostInteraction(post)
    data class Vote(override val post: PostModel, val vote : Int) : PostInteraction(post)
    data class Save(override val post: PostModel) : PostInteraction(post)
    data class PreviewUser(override val post: PostModel) : PostInteraction(post)
    data class VisitLink(override val post: PostModel) : PostInteraction(post)
    data class NewReply(override val post: PostModel) : PostInteraction(post)
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
