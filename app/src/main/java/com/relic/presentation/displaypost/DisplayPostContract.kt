package com.relic.presentation.displaypost

import com.relic.data.models.CommentModel

const val UPVOTE_PRESSED = 1
const val DOWNVOTE_PRESSED = -1

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
        fun onImagePressed()
        fun onReplyPressed()
    }
}

sealed class PostNavigationData {
    data class ToImage(
        val imageUrl : String
    ) : PostNavigationData()

    data class ToReply(
        val parentFullname : String
    ) : PostNavigationData()
}
