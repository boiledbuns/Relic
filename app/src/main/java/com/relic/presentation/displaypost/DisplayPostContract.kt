package com.relic.presentation.displaypost

import com.relic.data.models.CommentModel
import com.relic.data.models.ListingItem
import com.relic.util.MediaType

const val UPVOTE_PRESSED = 1
const val DOWNVOTE_PRESSED = -1

const val IMAGE = 1
const val LINK = 2

interface DisplayPostContract {

    interface ViewModel {
        fun refreshData()

        /**
         * Hook for view to tell the ViewModel to retrieve more comments
         * @param refresh whether the comments should be refreshed or not
         */
        fun retrieveMoreComments(refresh: Boolean = false)
    }

    interface PostViewDelegate {
        fun onExpandReplies(commentId: String, expanded : Boolean)
        fun onPostVoted(voteValue: Int)
        fun onCommentVoted(commentModel: CommentModel, voteValue: Int) : Int
        fun onLinkPressed()
        fun onReplyPressed()
        fun onUserPressed(listing: ListingItem)
    }

    interface CommentAdapterDelegate {
        fun displayCommentReplies(itemId : String, commentExpanded : Boolean)
        fun voteOnComment(itemPosition : Int, voteValue : Int)
        fun replyToComment(itemPosition : Int)
        fun visitComment(itemPosition : Int)
        fun previewUser(itemPosition : Int)
        fun loadMoreComments(itemPosition: Int, displayReplies : Boolean)
    }
}

sealed class PostNavigationData {
    data class ToMedia(
        val mediaType: MediaType,
        val mediaUrl: String
    ) : PostNavigationData()

    data class ToReply(
        val parentFullname: String
    ) : PostNavigationData()

    data class ToURL(
        val url: String
    ) : PostNavigationData()

    data class ToUserPreview(
        val username: String
    ) : PostNavigationData()

    // TODO add external
}

sealed class PostErrorData {
    object NetworkUnavailable : PostErrorData()
    object UnexpectedException : PostErrorData()
}
