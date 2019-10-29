package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.PostModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.FullPostView
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.displaysub.DisplaySubContract

class FullPostVH(
    private val fullPostView : FullPostView,
    private val commentItemAdapter: CommentItemAdapter,
    private val postInteractor: DisplaySubContract.PostAdapterDelegate
) : RecyclerView.ViewHolder(fullPostView), DisplaySubContract.PostViewDelegate {

    init {
        fullPostView.setOnClicks(this)
    }

    fun bind(post : PostModel) {
        fullPostView.setPost(post)
    }

    // region PostViewDelegate

    override fun onPostPressed() = postInteractor.visitPost(getPost(layoutPosition))
    override fun onPostSavePressed() = postInteractor.savePost(getPost(layoutPosition))
    override fun onPostUpvotePressed() = postInteractor.voteOnPost(getPost(layoutPosition), UPVOTE_PRESSED)
    override fun onPostDownvotePressed() = postInteractor.voteOnPost(getPost(layoutPosition), DOWNVOTE_PRESSED)
    override fun onPostReply() = postInteractor.onNewReplyPressed( getPost(layoutPosition))
    override fun onPostLinkPressed() = postInteractor.onLinkPressed(getPost(layoutPosition))
    override fun onUserPressed() = postInteractor.previewUser(getPost(layoutPosition))

    // endregion PostViewDelegate

    private fun getPost(postLayoutPosition : Int) = commentItemAdapter.getPost(postLayoutPosition)!!
}