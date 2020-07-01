package com.relic.interactor

import androidx.lifecycle.LiveData
import com.relic.data.CommentRepository
import com.relic.data.gateway.CommentGateway
import com.relic.domain.models.CommentModel
import com.relic.presentation.displaysub.NavigationData
import com.relic.presentation.util.RelicEvent
import com.shopify.livedataktx.SingleLiveData
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentInteractorImpl @Inject constructor(
    private val commentGateway: CommentGateway,
    private val commentRepo: CommentRepository
) : Contract.CommentAdapterDelegate, CoroutineScope {

    override val coroutineContext = Dispatchers.Main + SupervisorJob() + CoroutineExceptionHandler { context, e ->
        Timber.e(e, "caught exception")
    }

    private val _navigationLiveData = SingleLiveData<RelicEvent<NavigationData>>()
    override val navigationLiveData: LiveData<RelicEvent<NavigationData>> = _navigationLiveData

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
        launch(Dispatchers.Main) { commentGateway.voteOnComment(comment.fullName, comment.userUpvoted) }
    }

    private fun onReplyPressed(parent: CommentModel, text: String) {
        launch(Dispatchers.Main) { commentRepo.postComment(parent.fullName, text) }
    }

    private fun onPreviewUser(comment: CommentModel) {
        _navigationLiveData.postValue(RelicEvent(NavigationData.ToUserPreview(comment.author)))
    }

    private fun onExpandReplies(comment: CommentModel) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun onVisit(comment: CommentModel) {
        val sub = comment.subreddit ?: return
        val link = comment.linkFullname ?: return

        val toPost = NavigationData.ToPost(
            subredditName = sub,
            postFullname = link,
            commentId = comment.id
        )
        _navigationLiveData.postValue(RelicEvent(toPost))
        comment.visited = true
    }
}