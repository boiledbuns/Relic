package com.relic.presentation.displaypost

import com.relic.domain.models.CommentModel

interface DisplayPostContract {

    interface ViewModel {
        fun refreshData()
    }

    interface LoadMoreCommentsDelegate {
        // this should only be available on the display post fragment
        fun onExpandReplies(comment : CommentModel)
    }
}

sealed class PostErrorData {
    object NetworkUnavailable : PostErrorData()
    object UnexpectedException : PostErrorData()
}
