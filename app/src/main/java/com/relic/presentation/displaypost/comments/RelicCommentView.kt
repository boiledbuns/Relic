package com.relic.presentation.displaypost.comments

import android.content.Context
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.CommentModel
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.displaypost.CommentInteraction
import com.relic.presentation.displaypost.DOWNVOTE_PRESSED
import com.relic.presentation.displaypost.DisplayPostContract
import com.relic.presentation.displaypost.UPVOTE_PRESSED
import com.relic.presentation.helper.DateHelper
import kotlinx.android.synthetic.main.comment_item.view.*
import kotlinx.android.synthetic.main.inline_reply.view.*

class RelicCommentView (
    context: Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private var displayParent : Boolean = false
    private lateinit var replyAnchor : LinearLayout
    private var replyAction : (text : String) -> Unit = { }
    private lateinit var comment : CommentModel

    init {
        LayoutInflater.from(context).inflate(R.layout.comment_item, this)
        commentReplyView.setOnClickListener { openReplyEditor() }
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

    fun setComment(commentModel : CommentModel) {
        comment = commentModel

        updateVoteView()

        commentScoreView.text = resources.getString(R.string.comment_score, commentModel.score)
        commentFlairView.text = commentModel.authorFlairText
        commentAuthorView.text = commentModel.author
        commentModel.created?.let { commentCreatedView.text = DateHelper.getDateDifferenceString(it) }

        commentModel.edited?.let { commentCreatedView.setTextColor(resources.getColor(R.color.edited)) }
        commentBodyView.text = Html.fromHtml(Html.fromHtml(commentModel.body).toString())

        if (commentModel.isSubmitter) {
            commentAuthorView.setBackgroundResource(R.drawable.tag)
            commentAuthorView.background?.setTint(resources.getColor(R.color.discussion_tag))
        } else {
            commentAuthorView.setBackgroundResource(0)
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

    fun setOnReplyAction(action : (text : String) -> Unit) {
        replyAction = action
    }

    fun setViewDelegate(delegate: DisplayPostContract.CommentAdapterDelegate,  notifier : ItemNotifier) {
        commentUpvoteView.setOnClickListener {
            delegate.interact(comment, CommentInteraction.Upvote)
            notifier.notifyItem()
        }
        commentDownvoteView.setOnClickListener {
            delegate.interact(comment, CommentInteraction.Downvote)
            notifier.notifyItem()
        }
        commentAuthorView.setOnClickListener {
            delegate.interact(comment, CommentInteraction.PreviewUser)
        }
        setOnReplyAction { text ->
            delegate.interact(comment, CommentInteraction.NewReply(text))
        }
        setOnClickListener {
            delegate.interact(comment, CommentInteraction.Visit)
        }
    }

    // region update view
    private fun updateVoteView() {
        when (comment.userUpvoted) {
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

    }
    // endregion update view

    private fun openReplyEditor() {
        val inlineReply = RelicInlineReplyView(context).apply {
            // TODO set onclick for cancel and send + prompt user to save or not
            replyCancel.setOnClickListener {
                replyAnchor.removeAllViews()
            }

            replySend.setOnClickListener {
                replyAnchor.removeAllViews()
                replyAction(replyEditorView.text.toString())
            }
        }

        replyAnchor = commentReplyInlineAnchor.apply {
            addView(inlineReply)
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