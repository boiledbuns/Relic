package com.relic.presentation.displaypost.commentlist

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import kotlinx.android.synthetic.main.relic_more_comments_item.view.*

class RelicCommentMoreItemsView(
    context: Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout (context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.relic_more_comments_item, this)
    }

    fun displayLoadMore(replyCount : Int) {
        loadMoreItemText.text = resources.getString(R.string.load_comments, replyCount)
    }

    fun displayReplyDepth(depth : Int) {
        // width, height
        val indentParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            marginStart = resources.getDimensionPixelSize(R.dimen.padding_s) * depth
            bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_xxxs)
        }

        loadMoreItemRoot.layoutParams = indentParams
    }
}