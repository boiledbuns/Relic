package com.relic.presentation.displaypost.comments

import android.content.Context
import android.text.method.LinkMovementMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.CommentModel
import com.relic.interactor.CommentInteraction
import com.relic.interactor.Contract
import com.relic.presentation.base.ItemNotifier
import com.relic.presentation.helper.DateHelper
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.Markwon
import kotlinx.android.synthetic.main.comment_item.view.*
import kotlinx.android.synthetic.main.inline_reply.view.*

class RelicCommentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private val markwon = Markwon.builder(context)
        .usePlugin(object : AbstractMarkwonPlugin() {
            override fun processMarkdown(markdown: String): String {
                return markdown
                    .replace("&gt;".toRegex(), ">")
            }
        }).build()

    private lateinit var replyAnchor: LinearLayout
    private var replyAction: (text: String) -> Unit = { }
    private lateinit var comment: CommentModel

    private val defaultColor: Int
    private val downvotedColor: Int
    private val upvotedColor: Int

    init {
        LayoutInflater.from(context).inflate(R.layout.comment_item, this)
        commentReplyView.setOnClickListener { openReplyEditor() }

        defaultColor = context.resources.getColor(R.color.white)
        downvotedColor = context.resources.getColor(R.color.downvote)
        upvotedColor = context.resources.getColor(R.color.upvote)
    }

    fun setComment(commentModel: CommentModel) {
        comment = commentModel

        updateVoteView()

        commentScoreView.text = resources.getString(R.string.comment_score, commentModel.score)
        commentFlairView.text = commentModel.authorFlairText
        commentAuthorView.text = commentModel.author
        commentModel.created?.let { commentCreatedView.text = DateHelper.getDateDifferenceString(it) }

        commentModel.edited?.let { commentCreatedView.setTextColor(resources.getColor(R.color.edited)) }
        commentBodyView.movementMethod = LinkMovementMethod.getInstance()
        markwon.setMarkdown(commentBodyView, commentModel.body)

        if (commentModel.isSubmitter) {
            commentAuthorView.setBackgroundResource(R.drawable.tag)
            commentAuthorView.background?.setTint(resources.getColor(R.color.discussion_tag))
        } else {
            commentAuthorView.setBackgroundResource(0)
        }

        if (commentModel.replyCount > 0) {
            commentReplyCount.text = resources.getString(R.string.reply_count, commentModel.replyCount)
        }

        if (commentModel.depth >= 0) {
            displayReplyDepth(commentModel.depth)
        }

        awardsView.setAwards(comment.awards)
    }

    fun setOnReplyAction(action: (text: String) -> Unit) {
        replyAction = action
    }

    fun setViewDelegate(delegate: Contract.CommentAdapterDelegate, notifier: ItemNotifier) {
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
    }

    // region update view
    private fun updateVoteView() {
        when (comment.userUpvoted) {
            1 -> {
                commentUpvoteView.setColorFilter(upvotedColor)
                commentDownvoteView.setColorFilter(defaultColor)
            }
            0 -> {
                commentUpvoteView.setColorFilter(defaultColor)
                commentDownvoteView.setColorFilter(defaultColor)
            }
            -1 -> {
                commentUpvoteView.setColorFilter(defaultColor)
                commentDownvoteView.setColorFilter(downvotedColor)
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

    private fun displayReplyDepth(depth: Int) {
        // width, height
        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            marginStart = resources.getDimensionPixelSize(R.dimen.padding_s) * depth
            bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_xxxs)
            commentRoot.layoutParams = this
        }
    }
}