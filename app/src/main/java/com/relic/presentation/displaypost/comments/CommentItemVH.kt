package com.relic.presentation.displaypost.comments

import androidx.recyclerview.widget.RecyclerView
import com.relic.domain.models.CommentModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentItemVH (
    private val commentItem : RelicCommentView
): androidx.recyclerview.widget.RecyclerView.ViewHolder(commentItem) {
    private var commentId = ""

    fun initializeOnClicks(adapter : DisplayPostContract.CommentAdapterDelegate) {
        commentItem.apply {
            commentUpvoteView.setOnClickListener { adapter.voteOnComment(adapterPosition, UPVOTE_PRESSED) }
            commentDownvoteView.setOnClickListener { adapter.voteOnComment(adapterPosition, DOWNVOTE_PRESSED) }
            commentAuthorView.setOnClickListener { adapter.previewUser(adapterPosition) }
            setOnReplyAction { text -> adapter.replyToComment(adapterPosition, text) }
            setOnClickListener { adapter.visitComment(adapterPosition) }
        }
    }

    fun bindComment(commentModel : CommentModel) {
        commentId = commentModel.fullName
        commentItem.setPost(commentModel)
    }
}