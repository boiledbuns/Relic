package com.relic.presentation.displaypost

import com.relic.data.models.CommentModel

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
        fun onUserPressed(commentModel: CommentModel)
    }

    interface CommentAdapterDelegate {
        fun displayCommentReplies(itemId : String, commentExpanded : Boolean)
        fun voteOnComment(itemPosition : Int, voteValue : Int)
        fun replyToComment(itemPosition : Int)
        fun visitComment(itemPosition : Int)
        fun previewUser(itemPosition : Int)
    }
}

sealed class PostNavigationData {
    data class ToImage(
            val imageUrl: String
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
}

sealed class PostExceptionData : Exception() {
    object NoComments : PostExceptionData()
    object NetworkUnavailable : PostExceptionData()
    object UnexpectedException : PostExceptionData()
}

sealed class DisplayPostType {
    object Image : DisplayPostType()
    object Link : DisplayPostType()
}
