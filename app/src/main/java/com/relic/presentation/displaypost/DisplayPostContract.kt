package com.relic.presentation.displaypost

import android.arch.lifecycle.LiveData

import com.relic.data.models.CommentModel
import com.relic.data.models.PostModel

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
        fun onCommentVoted(commentFullName : String, voteValue: Int)
        fun onImagePressed()
    }

    companion object {
        val UPVOTE = 1
        val DOWNVOTE = -1
    }
}

sealed class PostNavigationData {
    data class ToImage (
            val imageUrl : String
    ) : PostNavigationData()
}
