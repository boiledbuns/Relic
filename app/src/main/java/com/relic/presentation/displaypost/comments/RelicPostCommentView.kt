package com.relic.presentation.displaypost.comments

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.CommentModel
import com.relic.interactor.CommentInteraction
import com.relic.interactor.Contract
import com.relic.presentation.base.ItemNotifier
import kotlinx.android.synthetic.main.post_comment_item.view.*

class RelicPostCommentView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    private lateinit var comment : CommentModel

    init {
        LayoutInflater.from(context).inflate(R.layout.post_comment_item, this)
    }

    fun setComment(commentModel: CommentModel) {
        comment = commentModel
        // update parent fields only
        parentTitle.text = commentModel.linkTitle
        parentAuthor.text = commentModel.linkAuthor
        parentSubreddit.text = commentModel.subreddit
    }

    fun setViewDelegate(delegate: Contract.CommentAdapterDelegate, notifier: ItemNotifier) {
        setOnClickListener {
            delegate.interact(comment, CommentInteraction.Visit)
        }
    }
    // endregion update view
}