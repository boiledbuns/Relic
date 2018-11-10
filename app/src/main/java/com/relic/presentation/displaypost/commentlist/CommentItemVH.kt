package com.relic.presentation.displaypost.commentlist

import android.os.Build
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import com.relic.R
import com.relic.data.models.CommentModel
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentItemVH (
        private val commentItem : View
): RecyclerView.ViewHolder(commentItem) {

    var commentPosition = 0

    fun initializeOnClicks(adapter : CommentItemAdapter) {
        commentItem.apply {
            commentUpvoteView.setOnClickListener { adapter.voteOnComment(commentPosition, UPVOTE_PRESSED) }
            commentDownvoteView.setOnClickListener { adapter.voteOnComment(commentPosition, DOWNVOTE_PRESSED) }
        }
    }

    fun bindComment(commentModel : CommentModel, position : Int) {
        commentPosition = position

        commentItem.apply {
            commentScoreView.text = commentModel.score.toString() + commentModel.id
            commentAuthorView.text = commentModel.author
            commentCreatedView.text = commentModel.created

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                commentBodyView.text = Html.fromHtml(Html.fromHtml(commentModel.body).toString())
            } else {
                commentBodyView.text = Html.fromHtml(commentModel.body)
            }

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
        }
    }
}