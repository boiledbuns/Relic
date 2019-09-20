package com.relic.presentation.search.subs

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.relic.domain.models.SubredditModel

class SearchSubItemView @JvmOverloads constructor(
        context: Context,
        attrs : AttributeSet? = null,
        defStyleAttr : Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    fun bindSubreddit(sub : SubredditModel) {

    }

}