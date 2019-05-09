package com.relic.presentation.displaypost.commentlist

import android.support.v7.widget.RecyclerView
import android.text.Html
import com.relic.R
import com.relic.data.models.CommentModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentItemVH (
    private val commentItem : RelicCommentView
): RecyclerView.ViewHolder(commentItem) {
    private var commentId = ""

    fun initializeOnClicks(adapter : DisplayPostContract.CommentAdapterDelegate) {
        commentItem.apply {
            commentUpvoteView.setOnClickListener { adapter.voteOnComment(adapterPosition, UPVOTE_PRESSED) }
            commentDownvoteView.setOnClickListener { adapter.voteOnComment(adapterPosition, DOWNVOTE_PRESSED) }
            commentAuthorView.setOnClickListener { adapter.previewUser(adapterPosition) }
            setOnClickListener { adapter.visitComment(adapterPosition) }
        }
    }

    fun bindComment(commentModel : CommentModel) {
        commentId = commentModel.fullName
        commentItem.setPost(commentModel)
    }
}