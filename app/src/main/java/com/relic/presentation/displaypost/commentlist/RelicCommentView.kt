package com.relic.presentation.displaypost.commentlist

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.relic.R
import com.relic.data.models.CommentModel
import kotlinx.android.synthetic.main.comment_item.view.*

class RelicCommentView (
    context: Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var displayParent : Boolean = false

    init {
        LayoutInflater.from(context).inflate(R.layout.comment_item, this)
    }

    fun displayParent(display : Boolean) {
        displayParent = display
        if (displayParent) {
            parentBlock.visibility = View.VISIBLE
            parent_separator.visibility = View.VISIBLE
            indentCommentIcon.visibility = View.VISIBLE
            // width, height
            LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = resources.getDimensionPixelSize(R.dimen.postitem_margin)
                bottomMargin = 0
                commentRoot.layoutParams = this
            }
        }
    }

    fun setPost(commentModel : CommentModel) {
        commentScoreView.text = commentModel.score.toString() + " " + commentModel.depth
        commentFlairView.text = " " + commentModel.position  + " " + commentModel.authorFlairText
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

        // update parent fields only if they are supposed to be displayed
        if (displayParent) {
            parentTitle.text = commentModel.linkTitle
            parentAuthor.text = commentModel.linkAuthor
            parentSubreddit.text = commentModel.subreddit
        }
        else if (commentModel.depth >= 0){
            displayReplyDepth(commentModel.depth)
        }
    }

    private fun displayReplyDepth(depth : Int) {
        // width, height
        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            marginStart = resources.getDimensionPixelSize(R.dimen.padding_s) * depth
            bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_xxxs)
            commentRoot.layoutParams = this
        }
    }
}