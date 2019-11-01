package com.relic.presentation.displaypost

import com.relic.domain.models.CommentModel

const val UPVOTE_PRESSED = 1
const val DOWNVOTE_PRESSED = -1

const val IMAGE = 1
const val LINK = 2

interface DisplayPostContract {

    interface ViewModel {
        fun refreshData()
        fun onExpandReplies(comment : CommentModel, expanded : Boolean)
    }

    interface CommentAdapterDelegate {
        fun onCommentVoted(comment: CommentModel, voteValue: Int)
        fun onReplyPressed(parent : CommentModel, text : String)
        fun onPreviewUser(comment: CommentModel)
        fun onExpandReplies(comment: CommentModel)
    }

    interface CommentViewDelegate {
        fun voteOnComment(voteValue : Int)
        fun replyToComment(text: String)
        fun visitComment()
        fun previewUser()
        fun loadMoreComments(displayReplies : Boolean)
    }
}

sealed class PostErrorData {
    object NetworkUnavailable : PostErrorData()
    object UnexpectedException : PostErrorData()
}
