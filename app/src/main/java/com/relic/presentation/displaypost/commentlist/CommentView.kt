package com.relic.presentation.displaypost.commentlist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import kotlinx.android.synthetic.main.comment_item.view.*

class CommentView (
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.comment_item, this)
    }

    fun displayReplyDepth(depth : Int) {
        // width, height
        LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
            marginStart = resources.getDimensionPixelSize(R.dimen.padding_s) * depth
            bottomMargin = resources.getDimensionPixelSize(R.dimen.margin_xxxs)
            commentRoot.layoutParams = this
        }
    }
}