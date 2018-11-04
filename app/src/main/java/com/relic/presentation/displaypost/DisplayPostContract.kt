package com.relic.presentation.displaypost

import com.relic.data.models.CommentModel

const val UPVOTE_PRESSED = 1
const val DOWNVOTE_PRESSED = -1

interface DisplayPostContract {

    interface ViewModel {
        /**
         * Hook for view to tell the VM to retrieve more comments
         * @param refresh whether the comments should be refreshed or not
         */
        fun retrieveMoreComments(refresh: Boolean)
        fun refresh()
    }

    interface PostViewDelegate {
        fun onPostVoted(voteValue: Int)
        fun onCommentVoted(commentModel: CommentModel, voteValue: Int) : Int
        fun onImagePressed()
    }
}

sealed class PostNavigationData {
    data class ToImage (
            val imageUrl : String
    ) : PostNavigationData()
}
