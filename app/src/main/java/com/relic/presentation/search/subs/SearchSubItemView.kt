package com.relic.presentation.search.subs

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.relic.R
import com.relic.domain.models.SubredditModel
import kotlinx.android.synthetic.main.sub_search_item.view.*

class SearchSubItemView @JvmOverloads constructor(
    context: Context,
    attrs : AttributeSet? = null,
    defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.sub_search_item, this, true)
    }

    fun bind(sub : SubredditModel) {
        subName.text = sub.subName
    }
}