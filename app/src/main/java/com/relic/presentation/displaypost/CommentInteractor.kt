package com.relic.presentation.displaypost

import com.relic.data.CommentRepository
import com.relic.data.gateway.CommentGateway
import com.relic.domain.models.CommentModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

class CommentInteractor @Inject constructor(
    private val commentGateway: CommentGateway,
    private val commentRepo: CommentRepository
) : DisplayPostContract.CommentAdapterDelegate, CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e,  "caught exception")
    }

    override fun interact(comment: CommentModel, interaction: CommentInteraction) {
        when (interaction) {
            CommentInteraction.Upvote -> onCommentVoted(comment, 1)
            CommentInteraction.Downvote -> onCommentVoted(comment, -1)
            is CommentInteraction.NewReply -> onReplyPressed(comment, interaction.text)
            CommentInteraction.PreviewUser -> onPreviewUser(comment)
            CommentInteraction.ExpandReplies -> onExpandReplies(comment)
            CommentInteraction.Visit -> onVisit(comment)
        }
    }

    private fun onCommentVoted(comment: CommentModel, vote: Int) {
        comment.userUpvoted = when (comment.userUpvoted) {
            1 -> if (vote == 1) 0 else -1
            -1 -> if (vote == -1) 0 else 1
            else -> vote
        }

        // send request only if value changed
        launch(Dispatchers.Main) { commentGateway.voteOnComment(comment.fullName, comment.userUpvoted)}
    }

    private fun onReplyPressed(parent: CommentModel, text: String) {
        launch(Dispatchers.Main) { commentRepo.postComment(parent.fullName, text)}
    }

    private fun onPreviewUser(comment: CommentModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun onExpandReplies(comment: CommentModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun onVisit(comment: CommentModel) {
        comment.visited = true
    }
}