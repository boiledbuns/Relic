package com.relic.presentation.displaypost.comments

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.CommentModel
import kotlinx.android.synthetic.main.post_comment_item.view.*

class RelicPostCommentView (
    context: Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.post_comment_item, this)
    }

    fun setComment(commentModel : CommentModel) {
        // update parent fields only
        parentTitle.text = commentModel.linkTitle
        parentAuthor.text = commentModel.linkAuthor
        parentSubreddit.text = commentModel.subreddit
    }

    // endregion update view
}