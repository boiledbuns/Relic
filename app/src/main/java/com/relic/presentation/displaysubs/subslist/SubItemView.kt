package com.relic.presentation.displaysubs.subslist

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.sub_item.view.*

class SubItemView @JvmOverloads constructor(
  context: Context,
  attrs : AttributeSet? = null,
  defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.sub_item, this, true)
    }

    fun bind(subredditModel : SubredditModel) {
        subreddit.text = subredditModel.subName
    }
}