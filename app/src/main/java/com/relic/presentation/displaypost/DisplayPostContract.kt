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

}

sealed class PostErrorData {
    object NetworkUnavailable : PostErrorData()
    object UnexpectedException : PostErrorData()
}
