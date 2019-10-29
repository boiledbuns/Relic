package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract

class CommentMoreItemsVH (
  private val moreCommentsItemView : RelicCommentMoreItemsView,
  private val commentItemAdapter: CommentItemAdapter,
  private val commentInteractor: DisplayPostContract.CommentAdapterDelegate
) : RecyclerView.ViewHolder(moreCommentsItemView), DisplayPostContract.CommentViewDelegate {

    init {
        moreCommentsItemView.setViewDelegate(this)
    }

    fun bind(commentModel : CommentModel){
        commentModel.more?.let { more ->
            if (more.isNotEmpty()) moreCommentsItemView.displayLoadMore(more.size)
        }
        moreCommentsItemView.displayReplyDepth(commentModel.depth)
    }

    // region comment view delegate

    override fun voteOnComment(voteValue: Int) = commentInteractor.onCommentVoted(getComment(layoutPosition), voteValue)
    override fun replyToComment(text: String) = commentInteractor.onReplyPressed(getComment(layoutPosition), text)
    override fun previewUser() = commentInteractor.onPreviewUser(getComment(layoutPosition))
    override fun loadMoreComments(displayReplies: Boolean) = commentInteractor.onExpandReplies(getComment(layoutPosition))
    override fun visitComment() {}

    // endregion comment view delegate

    private fun getComment(commentLayoutPosition: Int) = commentItemAdapter.getComment(commentLayoutPosition)
}