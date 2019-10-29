package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.api.adapter.CommentAdapter
import com.relic.domain.models.CommentModel
import com.relic.presentation.displaypost.DisplayPostContract

class CommentItemVH (
  private val commentView : RelicCommentView,
  private val commentAdapter: CommentItemAdapter,
  private val commentInteractor : DisplayPostContract.CommentAdapterDelegate
): RecyclerView.ViewHolder(commentView), DisplayPostContract.CommentViewDelegate{

    init {
        commentView.setViewDelegate(this)
    }

    fun bind(commentModel : CommentModel) {
        commentView.setPost(commentModel)
    }

    // region comment view delegate

    override fun voteOnComment(voteValue: Int) = commentInteractor.onCommentVoted(getComment(layoutPosition), voteValue)
    override fun replyToComment(text: String) = commentInteractor.onReplyPressed(getComment(layoutPosition), text)
    override fun previewUser() = commentInteractor.onPreviewUser(getComment(layoutPosition))
    override fun loadMoreComments(displayReplies: Boolean) = commentInteractor.onExpandReplies(getComment(layoutPosition))
    override fun visitComment() {}

    // endregion comment view delegate

    private fun getComment(commentLayoutPosition: Int) = commentAdapter.getComment(commentLayoutPosition)
}