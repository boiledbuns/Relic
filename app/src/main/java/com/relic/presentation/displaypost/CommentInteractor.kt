package com.relic.presentation.displaypost

import com.relic.data.CommentRepository
import com.relic.data.gateway.PostGateway
import com.relic.domain.models.CommentModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommentInteractor @Inject constructor(
    private val postGateway: PostGateway,
    private val commentRepo: CommentRepository
) : DisplayPostContract.CommentAdapterDelegate, CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e,  "caught exception")
    }

    override fun onCommentVoted(comment: CommentModel, voteValue: Int) : Int {
        var newUserUpvoteValue = 0
        when (voteValue) {
            UPVOTE_PRESSED -> {
                if (comment.userUpvoted != CommentModel.UPVOTE) newUserUpvoteValue = CommentModel.UPVOTE
            }
            DOWNVOTE_PRESSED -> {
                if (comment.userUpvoted != CommentModel.DOWNVOTE) newUserUpvoteValue = CommentModel.DOWNVOTE
            }
        }

        // send request only if value changed
        if (newUserUpvoteValue != comment.userUpvoted) {
            launch(Dispatchers.Main) { postGateway.voteOnPost(comment.fullName, newUserUpvoteValue) }
        }

        return newUserUpvoteValue
    }

    override fun onReplyPressed(parent: CommentModel, text: String) {
        launch(Dispatchers.Main) {
            commentRepo.postComment(parent.fullName, text)
        }
    }

    override fun onPreviewUser(comment: CommentModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExpandReplies(comment: CommentModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}