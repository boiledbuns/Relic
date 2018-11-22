package com.relic.presentation.displaypost.commentlist

import android.support.v7.widget.RecyclerView
import android.text.Html
import com.relic.R
import com.relic.data.models.CommentModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentItemVH (
        private val commentItem : CommentView
): RecyclerView.ViewHolder(commentItem) {
    private var commentsExpanded = false
    private var commentPosition = 0

    fun initializeOnClicks(adapter : CommentItemAdapter) {
        commentItem.apply {
            commentUpvoteView.setOnClickListener { adapter.voteOnComment(commentPosition, UPVOTE_PRESSED) }
            commentDownvoteView.setOnClickListener { adapter.voteOnComment(commentPosition, DOWNVOTE_PRESSED) }
            commentReplyCount.setOnClickListener {
                adapter.displayCommentReplies(commentPosition, commentsExpanded)
                // if showing replies, display a placeholder loading
                commentsExpanded = !commentsExpanded
            }
        }
    }

    fun bindComment(commentModel : CommentModel, position : Int) {
        commentPosition = position

        commentItem.apply {
            commentScoreView.text = commentModel.score.toString() + " " + commentModel.depth
            commentFlairView.text = commentModel.authorFlairText
            commentAuthorView.text = commentModel.author
            commentCreatedView.text = commentModel.created

            commentModel.edited?.let { commentCreatedView.setTextColor(resources.getColor(R.color.edited)) }
            commentBodyView.text = Html.fromHtml(Html.fromHtml(commentModel.body).toString())

            if (commentModel.isSubmitter) {
                commentAuthorView.setBackgroundResource(R.drawable.tag)
                commentAuthorView.background?.setTint(resources.getColor(R.color.discussion_tag))
            }

            when (commentModel.userUpvoted) {
                1 -> {
                    commentUpvoteView.setImageResource(R.drawable.ic_upvote_active)
                    commentDownvoteView.setImageResource(R.drawable.ic_downvote)
                }
                0 -> {
                    commentUpvoteView.setImageResource(R.drawable.ic_upvote)
                    commentDownvoteView.setImageResource(R.drawable.ic_downvote)
                }
                -1 -> {
                    commentUpvoteView.setImageResource(R.drawable.ic_upvote)
                    commentDownvoteView.setImageResource(R.drawable.ic_downvote_active)
                }
            }

            if (commentModel.replyCount > 0) {
                commentReplyCount.text = resources.getString(R.string.reply_count, commentModel.replyCount)
            }

            commentItem.displayReplyDepth(commentModel.depth)
        }
    }
}