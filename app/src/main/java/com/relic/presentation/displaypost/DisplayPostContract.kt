package com.relic.presentation.displaypost

import com.relic.domain.models.CommentModel
import com.relic.domain.models.ListingItem
import com.relic.presentation.util.MediaType

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

    interface CommentVHDelegate {
        fun voteOnComment(position: Int, voteValue : Int)
        fun replyToComment(position: Int, text: String)
        fun visitComment(position: Int)
        fun previewUser(position: Int)
        fun loadMoreComments(position: Int, displayReplies : Boolean)
    }

    interface PostVHDelegate {
        fun onPostPressed(position: Int)
        fun onPostSavePressed(position: Int)
        fun onPostUpvotePressed(position: Int)
        fun onPostDownvotePressed(position: Int)

        fun onPostReply(position: Int)
        fun onPostLinkPressed(position: Int)
        fun onUserPressed(position: Int)
    }

//    interface PostViewDelegate {
//        fun onExpandReplies(comment : CommentModel, expanded : Boolean)
//        fun onPostVoted(voteValue: Int)
//        fun onCommentVoted(commentModel: CommentModel, voteValue: Int) : Int
//        fun onLinkPressed()
//        fun onNewReplyPressed()
//        fun onReplyPressed(parent : String, text : String)
//        fun onUserPressed(listing: ListingItem)
//    }

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
